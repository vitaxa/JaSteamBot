package com.vitaxa.jasteambot.steam.trade;

import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.helper.ConcurrentHelper;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.event.Event;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.inventory.exception.TradeException;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Objects;
import java.util.concurrent.*;

public final class TradeManager {

    private static final ThreadFactory THREAD_FACTORY = r -> CommonHelper.newThread("TradeManager Thread", true, r);
    private static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool(THREAD_FACTORY);

    private static final Logger LOG = LoggerFactory.getLogger(TradeManager.class);

    // Default
    private static final int MAX_GAP_TIME_DEFAULT = 15;
    private static final int MAX_TRADE_TIME_DEFAULT = 180;
    private static final int TRADE_POLLING_INTERVAL_DEFAULT = 800;
    /**
     * Occurs when the trade times out because either the user didn't complete an
     * action in a set amount of time, or they took too long with the whole trade.
     */
    private final Event<Void> onTimeout = new Event<>();
    private final SteamWeb steamWeb;
    private int maxTradeTimeSec;
    private int maxActionGapSec;
    private int tradePollingInterval;
    // Unix time
    private long tradeStartTime;
    private long lastOtherActionTime;
    private long lastTimeoutMessage;
    private Inventory myInventory;
    private Inventory otherInventory;
    private volatile boolean isTradeRunning;

    /**
     * Initializes a new instance of the TradeManager class.
     *
     * @param apiKey   The Steam Web API key. Cannot be null.
     * @param steamWeb The SteamWeb instances for this bot
     */
    public TradeManager(String apiKey, SteamWeb steamWeb) {
        this.steamWeb = Objects.requireNonNull(steamWeb, "steamWeb");

        setTradeTimeLimits(MAX_TRADE_TIME_DEFAULT, MAX_GAP_TIME_DEFAULT, TRADE_POLLING_INTERVAL_DEFAULT);
    }

    /**
     * Sets the trade time limits.
     *
     * @param maxTradeTime    Max trade time in seconds.
     * @param maxActionGap    Max gap between user action in seconds.
     * @param pollingInterval The trade polling interval in milliseconds.
     */
    public void setTradeTimeLimits(int maxTradeTime, int maxActionGap, int pollingInterval) {
        this.maxTradeTimeSec = maxTradeTime;
        this.maxActionGapSec = maxActionGap;
        this.tradePollingInterval = pollingInterval;
    }

    /**
     * Creates a trade object and returns it for use.
     * Call {@code impl_initializeTrade} before using this method
     *
     * @param me    The SteamID of the bot.
     * @param other The SteamID of the other trade partner.
     * @return The trade object to use to interact with the Steam trade.
     * If the needed inventories are <c>null</c> then they will be fetched
     */
    @Deprecated
    public Trade createTrade(SteamID me, SteamID other) {
        return createTrade(me, other, GameType.CSGO);
    }

    /**
     * Creates a trade object and returns it for use.
     * Call {@code impl_initializeTrade} before using this method
     *
     * @param me       The SteamID of the bot.
     * @param other    The SteamID of the other trade partner.
     * @param gameType Game type for inventory load
     * @return The trade object to use to interact with the Steam trade.
     * If the needed inventories are <c>null</c> then they will be fetched
     */
    public Trade createTrade(SteamID me, SteamID other, GameType gameType) {
        if (otherInventory == null || myInventory == null) {
            try {
                impl_initializeTrade(me, other, gameType);
            } catch (TradeException e) {
                LOG.error("Can't initialize trade", e);
            }
        }

        Trade trade = new Trade(me, other, steamWeb, myInventory, otherInventory);

        trade.getOnCloseEvent().addEventListener(args -> {
            isTradeRunning = true;
        });

        return trade;
    }

    /**
     * Stops the trade thread.
     * Also, nulls out the inventory objects so they have to be fetched again if a new trade is started.
     */
    public void stopTrade() {
        otherInventory = null;
        myInventory = null;

        isTradeRunning = false;
    }

    /**
     * Starts the actual trade-polling thread.
     *
     * @param trade
     */
    public void startTrade(Trade trade) {
        // initialize data to use in thread
        tradeStartTime = CommonHelper.getUnixTimestamp();
        lastOtherActionTime = CommonHelper.getUnixTimestamp();
        lastTimeoutMessage = CommonHelper.getUnixTimestamp() - 1000;

        CommonHelper.newThread("TradeManager Process Thread", true, () -> {
            isTradeRunning = true;

            LOG.debug("Trade thread starting.");

            try {
                // main thread loop for polling
                while (isTradeRunning) {
                    boolean action = trade.poll();

                    if (action)
                        lastOtherActionTime = CommonHelper.getUnixTimestamp();

                    if (trade.hasTradeEnded() || checkTradeTimeout(trade)) {
                        isTradeRunning = false;
                        break;
                    }

                    ConcurrentHelper.sleepInMillis(tradePollingInterval);
                }
            } catch (Exception e) {
                LOG.error("[TRADEMANAGER] general error", e);
                isTradeRunning = false;
                trade.fireOnErrorEvent("Unknown error occurred: " + e.toString());
            } finally {
                LOG.debug("Trade thread shutting down.");

                if (trade.isTradeAwaitingConfirmation()) {
                    trade.fireOnAwaitingConfirmation();
                }
                trade.fireOnCloseEvent();
            }
        }).start();
    }

    /**
     * Fetch the inventories of both the bot and the other user.
     *
     * @param me       The SteamID of the bot.
     * @param other    The SteamID of the other trade partner.
     *                 This should be done anytime a new user is traded with or the inventories are out of date. It should
     *                 be done sometime before calling {@code createTrade}.
     * @param gameType Game type for inventory load
     */
    private void impl_initializeTrade(SteamID me, SteamID other, GameType gameType) throws TradeException {
        // fetch other player's inventory from the Steam API.
        final Callable<Inventory> otherInventoryTask = () -> {
            return Inventory.fetchInventory(other, steamWeb, gameType);
        };

        // fetch our inventory from the Steam API.
        final Callable<Inventory> myInventoryTask = () -> {
            return Inventory.fetchInventory(me, steamWeb, gameType);
        };

        final Future<Inventory> otherInventory = THREAD_POOL.submit(otherInventoryTask);
        final Future<Inventory> myInventory = THREAD_POOL.submit(myInventoryTask);

        try {
            this.otherInventory = otherInventory.get();
            this.myInventory = myInventory.get();
        } catch (InterruptedException e) {
            LOG.error("Initialize trade was interrupted, can't get other or my inventory");
        } catch (ExecutionException e) {
            LOG.error("Execution fail", e);
        }
    }

    private boolean checkTradeTimeout(Trade trade) {
        // User has accepted the trade. Disregard time out.
        if (trade.isOtherUserAccepted()) return false;

        long now = CommonHelper.getUnixTimestamp();

        long actionTimeout = lastOtherActionTime + maxActionGapSec;
        int untilActionTimeout = Math.round(actionTimeout - now);

        LOG.debug("{} {}", actionTimeout, untilActionTimeout);

        long tradeTimeout = tradeStartTime + maxTradeTimeSec;
        int untilTradeTimeout = Math.round(tradeTimeout - now);

        double secsSinceLastTimeoutMessage = now - lastTimeoutMessage;

        if (untilActionTimeout <= 0 || untilTradeTimeout <= 0) {
            LOG.debug("timed out...");

            onTimeout.handleEvent();

            trade.cancelTrade();

            return true;
        } else if (untilActionTimeout <= 20 && secsSinceLastTimeoutMessage >= 10) {
            trade.sendMessage("Are You AFK? The trade will be canceled in " + untilActionTimeout + " seconds if you don't do something.");
            lastTimeoutMessage = now;
        }

        return false;
    }

    public Event<Void> getOnTimeout() {
        return onTimeout;
    }
}
