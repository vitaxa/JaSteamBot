package com.vitaxa.jasteambot.command.console.basic;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;

public final class VersionCommand extends ConsoleCommand {
    public VersionCommand(JaSteamServer jasteam) {
        super(jasteam);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Print JaSteamBot version";
    }

    @Override
    public void invoke(String... args) throws Exception {
        log.info("JaSteamBot version: {}", JaSteamServer.VERSION);
    }
}

