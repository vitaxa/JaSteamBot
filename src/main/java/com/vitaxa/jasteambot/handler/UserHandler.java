package com.vitaxa.jasteambot.handler;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.BotConfig;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import com.vitaxa.jasteambot.steam.trade.Trade;
import com.vitaxa.jasteambot.steam.trade.offer.TradeOffer;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import org.slf4j.Logger;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Objects;
import java.util.function.Consumer;

public abstract class UserHandler {

    protected final Bot bot;
    protected final Logger log;
    protected final SteamID otherSID;
    private final BotConfig botConfig;
    protected SteamWeb steamWeb;
    private boolean lastMessageFromTrade;

    /**
     * The abstract base class for users of SteamBot that will allow a user
     * to extend the functionality of the Bot.
     */
    UserHandler(Bot bot, SteamID otherSID) {
        this.bot = Objects.requireNonNull(bot, "bot");
        this.botConfig = bot.getBotConfig();
        this.log = bot.getLog();

        this.otherSID = Objects.requireNonNull(otherSID, "steamid");

        if (this.bot.getSteamWeb() == null) {
            throw new IllegalArgumentException("You cannot use 'SteamWeb' before the Bot has been initialized!");
        }

        this.steamWeb = bot.getSteamWeb();
    }

    /**
     * @return Gets the Bot's current trade.
     */
    public Trade getCurrentTrade() {
        return bot.getCurrentTrade();
    }

    /**
     * @return Checks if the user is an admin
     */
    public boolean isAdmin() {
        return botConfig.getAdmins().contains(otherSID.convertToUInt64());
    }

    /**
     * Called when the user adds the bot as a friend.
     *
     * @return Whether to accept.
     */
    public abstract boolean onFriendAdd();

    /**
     * Called when the user removes the bot as a friend.
     *
     * @return Whether to accept.
     */
    public abstract boolean onFriendRemove();

    /**
     * Called whenever a message is sent to the bot.
     * This is limited to regular and emote messages.
     */
    public abstract void onMessage(String message, EChatEntryType type);

    public void onMessageHandler(String message, EChatEntryType type) {
        lastMessageFromTrade = false;
        onMessage(message, type);
    }

    /**
     * Called when the bot is fully logged in.
     */
    public abstract void onLoginCompleted();

    /**
     * Called whenever a user requests a trade.
     *
     * @return Whether to accept
     */
    public abstract boolean onTradeRequest();

    /**
     * Called when a trade offer is updated, including the first time it is seen.
     * When the bot is restarted, this might get called again for trade offers it's been previously called on.
     * this method being called only once after an offer is accepted!  If you need to rely on that functionality (say for giving users non-Steam currency),
     * you need to keep track of which trades have been paid out yourself
     *
     * @param offer
     */
    public abstract void onTradeOfferUpdated(TradeOffer offer);

    /**
     * Called when user accepts or denies bot's trade request.
     *
     * @param accepted True if user accepted bot's request, false if not.
     * @param response String response of the callback.
     */
    public abstract void onTradeRequestReply(boolean accepted, String response);

    // Trade Events

    public abstract void onTradeError(String error);

    public void onStatusError(Trade.TradeStatusType status) {
        String statusMessage = (getCurrentTrade() != null ? getCurrentTrade().getTradeStatusErrorString(status) : "died a horrible death");
        String errorMessage = String.format("Trade with %s (%s) %s", otherSID.render(true), otherSID.convertToUInt64(), statusMessage);
        onTradeError(errorMessage);
    }

    public abstract void onTradeTimeout();

    public void onTradeAwaitingConfirmation(long tradeOfferID) {
        onTradeAwaitingConfirm(tradeOfferID);
    }

    public abstract void onTradeAwaitingConfirm(long tradeOfferID);

    public void onTradeClose() {
        bot.closeTrade();
    }

    public abstract void onTradeInit();

    public abstract void onTradeAddItem(Item inventoryItem);

    public abstract void onTradeRemoveItem(Item inventoryItem);

    public abstract void onTradeMessage(String message);

    public final void onTradeReadyHandler(boolean ready) {
        getCurrentTrade().poll();
        onTradeReady(ready);
    }

    public abstract void onTradeReady(boolean ready);

    public final void onTradeAcceptHandler() {
        getCurrentTrade().poll();
        if (getCurrentTrade().isOtherIsReady() && getCurrentTrade().isMeIsReady()) {
            onTradeAccept();
        }
    }

    public abstract void onTradeAccept();

    // Send chat methods

    protected void sendChatMessage(String message, Object... args) {
        sendMessage(this::sendChatMessageImpl, String.format(message, args));
    }

    /**
     * A helper method for sending a chat message to the other user in the trade window.
     * If the trade has ended, nothing this does nothing
     *
     * @param message The message to send to the other user
     * @param args    Optional. The format parameters, using the same syntax as string.format()
     */
    protected void sendTradeMessage(String message, Object... args) {
        sendMessage(this::sendTradeMessageImpl, String.format(message, args));
    }

    private void sendMessage(Consumer<String> action, String message) {
        action.accept(message);
    }

    private void sendChatMessageImpl(String message) {
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, message);
    }

    private void sendTradeMessageImpl(String message) {
        if (message.length() > 100) {
            log.warn("{} is longer than 100 chars, it will be trimmed.", message);
        }

        if (getCurrentTrade() != null && !getCurrentTrade().hasTradeEnded()) {
            getCurrentTrade().sendMessage(message);
        }
    }
}
