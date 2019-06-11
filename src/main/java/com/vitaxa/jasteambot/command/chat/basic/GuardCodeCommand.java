package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

public final class GuardCodeCommand extends BotCommand {

    public GuardCodeCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        final String steamGuardCode = bot.getMobileAuthCode();
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, steamGuardCode);
    }

    @Override
    public String commandName() {
        return "guardCode";
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Return SteamGuard code";
    }
}
