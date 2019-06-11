package com.vitaxa.jasteambot.command.console.basic;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;

public final class StopCommand extends ConsoleCommand {
    public StopCommand(JaSteamServer jasteam) {
        super(jasteam);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Stop JaSteamBot";
    }

    @Override
    public void invoke(String... args) {
        Runtime.getRuntime().exit(0);
    }
}
