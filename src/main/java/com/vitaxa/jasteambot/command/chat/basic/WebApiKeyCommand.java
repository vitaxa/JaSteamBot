package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

public final class WebApiKeyCommand extends BotCommand {

    public WebApiKeyCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        final String apiKey = bot.getSteamCommunity().getWebApiKey(args[0]);
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, apiKey);
    }

    @Override
    protected boolean beforeExecuteCheck(String... args) {
        if (args == null || args.length == 0) {
            bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, "Need to specify domain name");
            return false;
        }
        return true;
    }

    @Override
    public String commandName() {
        return "webApiKey";
    }

    @Override
    public String getArgsDescription() {
        return "[domain]";
    }

    @Override
    public String getUsageDescription() {
        return "Get web api key";
    }
}
