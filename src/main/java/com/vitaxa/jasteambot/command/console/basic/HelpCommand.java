package com.vitaxa.jasteambot.command.console.basic;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.CommandException;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;

import java.util.Map;

public final class HelpCommand extends ConsoleCommand {

    public HelpCommand(JaSteamServer jasteam) {
        super(jasteam);
    }

    @Override
    public String getArgsDescription() {
        return "[command name]";
    }

    @Override
    public String getUsageDescription() {
        return "Print command usage";
    }

    @Override
    public void invoke(String... args) throws CommandException {
        if (args.length < 1) {
            printCommands();
            return;
        }

        // Print command help
        printCommand(args[0]);
    }

    private void printCommand(String name) throws CommandException {
        printCommand(name, jasteam.getCommandHandler().lookup(name));
    }

    private void printCommands() {
        for (Map.Entry<String, ConsoleCommand> entry : jasteam.getCommandHandler().commandsMap().entrySet()) {
            printCommand(entry.getKey(), entry.getValue());
        }
    }

    private void printCommand(String name, ConsoleCommand consoleCommand) {
        String args = consoleCommand.getArgsDescription();
        log.info("{} {} - {}", name, args == null ? "[nothing]" : args, consoleCommand.getUsageDescription());
    }
}
