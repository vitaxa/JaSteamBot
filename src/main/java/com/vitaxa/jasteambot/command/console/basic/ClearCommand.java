package com.vitaxa.jasteambot.command.console.basic;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;

public final class ClearCommand extends ConsoleCommand {
    public ClearCommand(JaSteamServer jasteam) {
        super(jasteam);
    }

    @Override
    public String getArgsDescription() {
        return null;
    }

    @Override
    public String getUsageDescription() {
        return "Clear terminal";
    }

    @Override
    public void invoke(String... args) throws Exception {
        jasteam.getCommandHandler().clear();
        log.info("Terminal cleared");
    }
}
