package com.vitaxa.jasteambot.command.console;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ConsoleCommand implements Command {
    protected final JaSteamServer jasteam;
    protected final Logger log;

    protected ConsoleCommand(JaSteamServer jasteam) {
        this.jasteam = jasteam;
        this.log = LoggerFactory.getLogger(getClass());
    }

    protected final void verifyArgs(String[] args, int min) throws CommandException {
        if (args.length < min) {
            throw new CommandException("ConsoleCommand usage: " + getArgsDescription());
        }
    }
}
