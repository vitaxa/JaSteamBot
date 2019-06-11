package com.vitaxa.jasteambot.command.chat;

import com.vitaxa.jasteambot.Bot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BotCommandExecutor implements CommandExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(BotCommandExecutor.class);

    private final Bot bot;
    private final SteamID otherSID;

    private final Set<BotCommand> commonCommandSet = new HashSet<>();

    private final Set<BotCommand> adminCommandSet = new HashSet<>();

    public BotCommandExecutor(Bot bot, SteamID otherSID) {
        this.bot = bot;
        this.otherSID = otherSID;
    }

    @Override
    public void executeCommand(String command) throws CommandExecuteException {
        final String[] args = parse(command);
        if (args.length == 0) {
            return;
        }

        final String cmd = args[0];
        final Set<BotCommand> commandSet = isAdmin() ? adminCommandSet : commonCommandSet;

        final BotCommand botCommand = commandSet.stream()
                .filter(cmdStream -> cmdStream.commandName().equalsIgnoreCase(cmd))
                .findFirst()
                .orElseGet(() -> {
                    // Try to find in another set
                    if (isAdmin()) {
                        return commonCommandSet.stream()
                                .filter(commonCmdStream -> commonCmdStream.commandName().equalsIgnoreCase(cmd))
                                .findFirst().orElse(null);
                    }
                    return null;
                });

        if (botCommand == null) {
            throw new CommandExecuteException("Unknown command: " + cmd);
        }

        try {
            if (args.length >= 2) {
                botCommand.invoke(Arrays.copyOfRange(args, 1, args.length));
            } else {
                botCommand.invoke();
            }
        } catch (Exception e) {
            LOG.error("Exception while executing command: " + botCommand.commandName(), e);
            throw new CommandExecuteException("Couldn't invoke command: " + cmd, e);
        }
    }

    private String[] parse(String command) {
        if (command.contains("/")) {
            command = command.substring(command.indexOf("/") + 1);
        }

        Collection<String> result = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < command.length() + 1; i++) {
            boolean end = i >= command.length();
            char ch = end ? 0 : command.charAt(i);

            if (end || Character.isWhitespace(ch)) {
                if (builder.length() > 0) {
                    result.add(builder.toString());
                }

                builder.setLength(0);
                continue;
            }

            switch (ch) {
                case '\\':
                    char next = command.charAt(i + 1);
                    builder.append(next);
                    i++;
                    break;
                default:
                    builder.append(ch);
                    break;
            }
        }

        return result.toArray(new String[result.size()]);
    }

    public void registerCommand(BotCommand botCommand) {
        registerCommand(botCommand, false);
    }

    public void registerCommand(BotCommand botCommand, boolean adminOnly) {
        if (adminOnly) {
            commonCommandSet.remove(botCommand);
            adminCommandSet.add(botCommand);
        } else {
            adminCommandSet.remove(botCommand);
            commonCommandSet.add(botCommand);
        }
    }

    public List<BotCommand> getCommandList() {
        return Collections.unmodifiableList(
                Stream.concat(commonCommandSet.stream(), adminCommandSet.stream()).collect(Collectors.toList())
        );
    }

    private boolean isAdmin() {
        return bot.getBotConfig().getAdmins().contains(otherSID.convertToUInt64());
    }

    public Bot getBot() {
        return bot;
    }

}
