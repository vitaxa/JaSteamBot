package com.vitaxa.jasteambot.command.console.handler;

import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.CommandException;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;
import com.vitaxa.jasteambot.command.console.basic.*;
import com.vitaxa.jasteambot.helper.VerifyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CommandHandler implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(CommandHandler.class);

    private final Map<String, ConsoleCommand> commands = new ConcurrentHashMap<>(32);

    protected CommandHandler(JaSteamServer jasteam) {
        // Register basic commands
        registerCommand("help", new HelpCommand(jasteam));
        registerCommand("version", new VersionCommand(jasteam));
        registerCommand("stop", new StopCommand(jasteam));
        registerCommand("debug", new DebugCommand(jasteam));
        registerCommand("manage", new BotManagerCommand(jasteam));
    }

    private static String[] parse(CharSequence line) throws CommandException {
        boolean quoted = false;
        boolean wasQuoted = false;

        // Read line char by char
        Collection<String> result = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < line.length() + 1; i++) {
            boolean end = i >= line.length();
            char ch = end ? 0 : line.charAt(i);

            // Maybe we should read next argument?
            if (end || !quoted && Character.isWhitespace(ch)) {
                if (end && quoted) { // Quotes should be closed
                    throw new CommandException("Quotes wasn't closed");
                }

                // EMPTY args are ignored (except if was quoted)
                if (wasQuoted || builder.length() > 0) {
                    result.add(builder.toString());
                }

                // Reset string builder
                wasQuoted = false;
                builder.setLength(0);
                continue;
            }

            // Append next char
            switch (ch) {
                case '"': // "abc"de, "abc""de" also allowed
                    quoted = !quoted;
                    wasQuoted = true;
                    break;
                case '\\': // All escapes, including spaces etc
                    char next = line.charAt(i + 1);
                    builder.append(next);
                    i++;
                    break;
                default: // Default char, simply append
                    builder.append(ch);
                    break;
            }
        }

        // Return result as array
        return result.toArray(new String[result.size()]);
    }

    @Override
    public final void run() {
        try {
            readLoop();
        } catch (IOException e) {
            LOG.error("ConsoleCommand handler loop exception", e);
        }
    }

    public abstract void bell() throws IOException;

    public abstract void clear() throws IOException;

    public final Map<String, ConsoleCommand> commandsMap() {
        return Collections.unmodifiableMap(commands);
    }

    public final void eval(String line, boolean bell) {
        Instant startTime = Instant.now();
        try {
            String[] args = parse(line);
            if (args.length == 0) {
                return;
            }

            // Invoke command
            LOG.info("ConsoleCommand '{}'", line);
            lookup(args[0]).invoke(Arrays.copyOfRange(args, 1, args.length));
        } catch (Exception e) {
            LOG.error("ConsoleCommand invoke exception", e);
        }

        // Bell if invocation took > 1s
        Instant endTime = Instant.now();
        if (bell && Duration.between(startTime, endTime).getSeconds() >= 5) {
            try {
                bell();
            } catch (IOException e) {
                LOG.error("Bell failed", e);
            }
        }
    }

    public final ConsoleCommand lookup(String name) throws CommandException {
        ConsoleCommand consoleCommand = commands.get(name);
        if (consoleCommand == null) {
            throw new CommandException(String.format("Unknown consoleCommand: '%s'", name));
        }
        return consoleCommand;
    }

    public abstract String readLine() throws IOException;

    public final void registerCommand(String name, ConsoleCommand consoleCommand) {
        VerifyHelper.putIfAbsent(commands, name, Objects.requireNonNull(consoleCommand, "consoleCommand"),
                String.format("Command has been already registered: '%s'", name));
    }

    private void readLoop() throws IOException {
        for (String line = readLine(); line != null; line = readLine()) {
            eval(line, true);
        }
    }
}
