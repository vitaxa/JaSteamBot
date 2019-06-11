package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import com.vitaxa.jasteambot.steam.web.model.TradeUrl;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

public final class TradeUrlCommand extends BotCommand {

    public TradeUrlCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        final TradeUrl tradeURL = bot.getSteamCommunity().getTradeURL();
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, tradeURL.getUrl());
    }

    @Override
    public String commandName() {
        return "tradeUrl";
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Show trade url";
    }
}
