package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Collection;

public final class HelpCommand extends BotCommand {

    private final Collection<BotCommand> commandCollection;

    public HelpCommand(Bot bot, SteamID otherSID, Collection<BotCommand> commandCollection) {
        super(bot, otherSID);
        this.commandCollection = commandCollection;
    }

    @Override
    protected void execute(String... args) {
        if (args.length < 1) {
            printCommands();
            return;
        }
        printCommand(args[0]);
    }

    private void printCommands() {
        final StringBuilder sb = new StringBuilder();
        for (BotCommand command : commandCollection) {
            sb.append(buildCommandMessage(command));
        }
        bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, sb.toString());
    }

    private void printCommand(String name) {
        commandCollection.stream()
                .filter(command -> command.commandName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(command -> {
                    bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, buildCommandMessage(command));
                });
    }

    private String buildCommandMessage(BotCommand command) {
        return buildCommandMessage(command, new StringBuilder(), false);
    }

    private String buildCommandMessage(BotCommand command, StringBuilder sb, boolean tab) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        final String args = command.getArgsDescription();
        final String msg;
        if (tab) {
            msg = String.format(System.lineSeparator() + "\t %s %s - %s",
                    command.commandName(), args == null ? "[nothing]" : args, command.getUsageDescription());
        } else {
            msg = String.format(System.lineSeparator() + "%s %s - %s",
                    command.commandName(), args == null ? "[nothing]" : args, command.getUsageDescription());
        }
        sb.append(msg);
        for (BotCommand childCommand : command.getChildCommand()) {
            buildCommandMessage(childCommand, sb, true);
        }
        return sb.toString();
    }

    @Override
    public String commandName() {
        return "help";
    }

    @Override
    public String getArgsDescription() {
        return "[command name]";
    }

    @Override
    public String getUsageDescription() {
        return "Print command usage";
    }
}
