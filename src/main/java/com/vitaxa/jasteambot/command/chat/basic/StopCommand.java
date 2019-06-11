package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import uk.co.thomasc.steamkit.types.SteamID;

public final class StopCommand extends BotCommand {

    public StopCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
    }

    @Override
    protected void execute(String... args) {
        bot.stop();
    }

    @Override
    public String commandName() {
        return "stop";
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Shutdown bot";
    }
}
