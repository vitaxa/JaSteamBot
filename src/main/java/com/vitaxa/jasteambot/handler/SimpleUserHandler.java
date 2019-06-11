package com.vitaxa.jasteambot.handler;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommandExecutor;
import com.vitaxa.jasteambot.command.chat.CommandExecuteException;
import com.vitaxa.jasteambot.command.chat.basic.*;
import com.vitaxa.jasteambot.steam.inventory.exception.TradeException;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import com.vitaxa.jasteambot.steam.inventory.model.ItemDescription;
import com.vitaxa.jasteambot.steam.trade.offer.TradeOffer;
import com.vitaxa.jasteambot.steam.trade.offer.model.TradeOfferAcceptResponse;
import com.vitaxa.jasteambot.steam.web.model.ItemMarketPrice;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

public class SimpleUserHandler extends UserHandler {

    private BotCommandExecutor botCommandExecutor;

    public SimpleUserHandler(Bot bot, SteamID sid) {
        super(bot, sid);
        botCommandExecutor = new BotCommandExecutor(bot, sid);
        botCommandExecutor.registerCommand(new DebugCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new WebApiKeyCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new GuardCodeCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new InviteCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new StopCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new TradeCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new TradeUrlCommand(bot, otherSID), true);
        botCommandExecutor.registerCommand(new ProfileCommand(bot, otherSID));
        botCommandExecutor.registerCommand(new HelpCommand(bot, otherSID, botCommandExecutor.getCommandList()), true);
    }

    @Override
    public boolean onFriendAdd() {
        return true;
    }

    @Override
    public boolean onFriendRemove() {
        return false;
    }

    @Override
    public void onMessage(String message, EChatEntryType type) {
        final boolean isCommand = message.substring(0, 1).equalsIgnoreCase("/");
        if (isCommand) {
            bot.getLog().info("Command \"{}\" from {}", message, otherSID.convertToUInt64());
            executeCommand(message);
            return;
        }
        sendChatMessage(bot.getBotConfig().getChatResponse());
    }

    @Override
    public void onLoginCompleted() {
    }

    @Override
    public boolean onTradeRequest() {
        return true;
    }

    @Override
    public void onTradeOfferUpdated(TradeOffer offer) {
        switch (offer.getOfferState()) {
            case NEEDSCONFIRMATION:
                log.info("Trying to confirm trade offer");
                bot.acceptMobileTradeConfirmation(offer);
                break;
            case ACTIVE:
                if (offer.isOurOffer()) break;

                if (!isAdmin()) {
                    offer.cancel();
                    break;
                }

                log.info("Trying to accept active trade offer");

                TradeOfferAcceptResponse tradeOfferResponse = offer.accept();
                if (!tradeOfferResponse.getTradeError().isEmpty()) {
                    log.error("Error when confirming the trade offer: {}", tradeOfferResponse.getTradeError());
                    break;
                }
                if (tradeOfferResponse.isAccepted()) {
                    bot.acceptAllMobileTradeConfirmations();
                    log.info("Trade offer successfully accepted: Trade ID: {}", tradeOfferResponse.getTradeId());
                }
                break;
            case ACCEPTED:
                log.info("Trade offer {} from {} has been completed!", offer.getTradeOfferId(), offer.getPartnerSteamId().convertToUInt64());
                break;
            case INESCROW:
                log.info("Trade is still active but incomplete");
                break;
            case COUNTERED:
                log.info("Trade offer {} from {} was countered", offer.getTradeOfferId(), offer.getPartnerSteamId().convertToUInt64());
                break;
            default:
                log.warn("Trade offer {} from {} failed. Trade state: {}", offer.getTradeOfferId(),
                        offer.getPartnerSteamId().convertToUInt64(), offer.getOfferState().toString());
        }
    }

    @Override
    public void onTradeRequestReply(boolean accepted, String response) {
    }

    @Override
    public void onTradeError(String error) {
        sendChatMessage("Oh, there was an error: %s.", error);
        log.warn(error);
    }

    @Override
    public void onTradeTimeout() {
        sendChatMessage("Sorry, but you were AFK and the trade was canceled.");
        log.warn("User was kicked because he was AFK.");
    }

    @Override
    public void onTradeAwaitingConfirm(long tradeOfferID) {
        log.warn("Trade ended awaiting confirmation");
        sendChatMessage("Please complete the confirmation to finish the trade");
    }

    @Override
    public void onTradeInit() {
        log.info("Initialize trade with {} ({})", otherSID.render(true), otherSID.convertToUInt64());
        sendTradeMessage("Please put up your items.");
    }

    @Override
    public void onTradeAddItem(Item inventoryItem) {
        final ItemDescription description = inventoryItem.getDescription();
        final ItemMarketPrice marketPrice = bot.getSteamCommunity().getMarketPrice(1, description.getMarketHashName(),
                description.getAppId());
        if (marketPrice != null) {
            sendTradeMessage(String.format("You added %s. It's price %s", description.getName(), marketPrice.getMedianPrice()));
        }
    }

    @Override
    public void onTradeRemoveItem(Item inventoryItem) {
        final ItemDescription description = inventoryItem.getDescription();
        sendTradeMessage(String.format("You removed %s", description.getName()));
    }

    @Override
    public void onTradeMessage(String message) {
        if (isAdmin() && message.substring(0, 1).equalsIgnoreCase("/")) {
            executeCommand(message);
        }
    }

    @Override
    public void onTradeReady(boolean ready) {
        try {
            getCurrentTrade().setReady(ready);
        } catch (TradeException e) {
            log.error("Can't change trade state", e);
        }
    }

    @Override
    public void onTradeAccept() {
        // Even if it is successful, AcceptTrade can fail on
        // trades with a lot of items so we use a try-catch
        try {
            if (getCurrentTrade().acceptTrade()) {
                log.info("Trade Accepted!");
            } else {
                log.warn("Can't trade accept");
                getCurrentTrade().cancelTrade();
            }
        } catch (TradeException e) {
            log.warn("The trade might have failed, but we can't be sure.", e);
        }
    }

    private void executeCommand(String cmd) {
        try {
            botCommandExecutor.executeCommand(cmd);
        } catch (CommandExecuteException e) {
            sendChatMessage(e.getMessage());
            bot.getLog().warn("Couldn't execute command", e);
        }
    }
}
