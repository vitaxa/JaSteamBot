package com.vitaxa.jasteambot.command.console.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.BotManager;
import com.vitaxa.jasteambot.JaSteamServer;
import com.vitaxa.jasteambot.command.console.CommandException;
import com.vitaxa.jasteambot.command.console.ConsoleCommand;
import com.vitaxa.jasteambot.helper.CommonHelper;

import java.util.*;
import java.util.function.Consumer;

public final class BotManagerCommand extends ConsoleCommand {

    private final BotManager botManager;

    private final Set<BotManagerOptions> commands = new HashSet<>();

    public BotManagerCommand(JaSteamServer jasteam) {
        super(jasteam);
        botManager = Objects.requireNonNull(jasteam.getBotManager(), "botManager can't be null");
        registerCommand(new BotManagerOptions<String>("start", "start (X) where X = the username or index of the configured bot",
                val -> {
                    if (CommonHelper.isNumberOnly(val)) {
                        botManager.startByIndex(Integer.parseInt(val));
                    } else {
                        botManager.startByName(val);
                    }
                }));
        registerCommand(new BotManagerOptions<String>("stop", "stop (X) where X = the username or index of the configured bot",
                val -> {
                    if (CommonHelper.isNumberOnly(val)) {
                        botManager.stopByIndex(Integer.parseInt(val));
                    } else {
                        botManager.stopByName(val);
                    }
                }));
        registerCommand(new BotManagerOptions<Void>("stopall", "stop all bots", (o) -> botManager.stopAll()));
        registerCommand(new BotManagerOptions<Void>("help", "show all available commands", (o) -> printCommands()));

        // Mobile auth commands
        registerCommand(new BotManagerOptions<String>("linkAuth", "Link a mobile authenticator to bot account",
                this::linkAuth));
        registerCommand(new BotManagerOptions<String>("unlinkAuth", "Unlink a mobile authenticator",
                this::unlinkAuth));
        registerCommand(new BotManagerOptions<String>("getAuth", "Get a Steam Guard code for the account",
                this::getAuth));

        registerCommand(new BotManagerOptions<Void>("printAll", "Show all running bots", aVoid -> printAll()));
    }

    @Override
    public String getArgsDescription() {
        return "[command name]";
    }

    @Override
    public String getUsageDescription() {
        return "Commands for bot management";
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(String... args) throws Exception {
        if (args.length < 1) {
            printCommands();
            return;
        }

        // Search for command
        BotManagerOptions options = lookup(args[0]);

        // Execute function
        String argument = args.length > 1 ? args[1] : null;
        options.execute(argument);
    }

    private BotManagerOptions lookup(String name) throws CommandException {
        for (BotManagerOptions option : commands) {
            if (option.name.equalsIgnoreCase(name)) {
                return option;
            }
        }
        throw new CommandException(String.format("Unknown command: '%s'", name));
    }

    private void registerCommand(BotManagerOptions options) {
        commands.add(options);
    }

    private void printCommands() {
        for (BotManagerOptions options : commands) {
            log.info("{} - {}", options.name, options.help);
        }
    }

    private void linkAuth(String name) {
        final Optional<Bot> botOptional = botManager.findBotByName(name);
        botOptional.ifPresent(Bot::linkMobileAuth);
    }

    private void unlinkAuth(String name) {
        final Optional<Bot> botOptional = botManager.findBotByName(name);
        if (botOptional.isPresent()) {
            final Bot bot = botOptional.get();
            if (bot.getSteamGuardAccount() == null) {
                log.error("Mobile authenticator is not active on this bot.");
            } else if (bot.getSteamGuardAccount().deactivateAuthenticator()) {
                log.info("Deactivated authenticator on this account.");
            } else {
                log.error("Failed to deactivate authenticator on this account.");
            }
        }
    }

    private void getAuth(String name) {
        final Optional<Bot> botOptional = botManager.findBotByName(name);
        if (botOptional.isPresent()) {
            final Bot bot = botOptional.get();
            try {
                bot.getMobileAuthCode();
            } catch (Exception e) {
                bot.getLog().info("Unable to generate Steam Guard code.");
            }
        }
    }

    private void printAll() {
        final Map<Integer, Bot> runningBots = botManager.getRunningBots();
        botManager.getAllBots().forEach((integer, botData) -> {
            final Bot runningBot = botManager.getRunningBots().get(botData.getId());
            if (runningBot != null) {
                log.info("{} - RUNNING", botData.getUsername());
            } else {
                log.info("{} - AVAILABLE", botData.getUsername());
            }
        });
    }

    private static final class BotManagerOptions<T> {
        private final String name;
        private final String help;
        private final Consumer<T> func;

        BotManagerOptions(String name, String help, Consumer<T> func) {
            this.name = Objects.requireNonNull(name, "name");
            this.help = Objects.requireNonNull(help, "help");
            this.func = Objects.requireNonNull(func, "func");
        }

        void execute(T arg) {
            func.accept(arg);
        }
    }
}
