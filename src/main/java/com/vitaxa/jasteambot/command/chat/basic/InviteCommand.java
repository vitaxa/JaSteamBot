package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import com.vitaxa.jasteambot.helper.CommonHelper;
import uk.co.thomasc.steamkit.types.SteamID;

public final class InviteCommand extends BotCommand {

    public InviteCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        for (String arg : args) {
            if (CommonHelper.isNumeric(arg)) {
                bot.getSteamFriends().addFriend(new SteamID(Long.parseLong(arg)));
            }
        }
    }

    @Override
    protected boolean beforeExecuteCheck(String... args) {
        return args != null && args.length != 0;
    }

    @Override
    public String commandName() {
        return "invite";
    }

    @Override
    public String getArgsDescription() {
        return "[steamID]";
    }

    @Override
    public String getUsageDescription() {
        return "Friend invite";
    }
}
