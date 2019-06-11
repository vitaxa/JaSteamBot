package com.vitaxa.jasteambot.command.console.basic;

import ch.qos.logback.classic.Level;
import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;
import com.vitaxa.jasteambot.service.log.LogManager;

public final class DebugCommand extends ConsoleCommand {

    public DebugCommand(JaSteamServer jasteam) {
        super(jasteam);
    }

    @Override
    public String getArgsDescription() {
        return "[true/false]";
    }

    @Override
    public String getUsageDescription() {
        return "Enable or disable debug logging at runtime";
    }

    @Override
    public void invoke(String... args) {
        boolean newValue;
        if (args.length >= 1) {
            newValue = Boolean.parseBoolean(args[0]);
            LogManager.getInstance().setLoggingLevel(newValue ? Level.DEBUG : Level.INFO);
            jasteam.getBotManager().getRunningBots().forEach((integer, bot) -> {
                LogManager.getInstance().setLoggingLevel(newValue ? Level.DEBUG : Level.INFO, bot.getBotConfig().getUsername());
            });
        } else {
            newValue = LogManager.getInstance().getLoggerLevel() == Level.DEBUG;
        }
        log.info("Debug enabled: " + newValue);
    }
}

