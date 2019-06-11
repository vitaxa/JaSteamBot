package com.vitaxa.jasteambot;

import ch.qos.logback.classic.Level;
import com.vitaxa.jasteambot.command.console.handler.CommandHandler;
import com.vitaxa.jasteambot.command.console.handler.JLineCommandHandler;
import com.vitaxa.jasteambot.command.console.handler.StdCommandHandler;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.helper.VerifyHelper;
import com.vitaxa.jasteambot.serialize.config.ConfigObject;
import com.vitaxa.jasteambot.serialize.config.TextConfigReader;
import com.vitaxa.jasteambot.serialize.config.TextConfigWriter;
import com.vitaxa.jasteambot.serialize.config.entry.*;
import com.vitaxa.jasteambot.service.log.LogManager;
import com.vitaxa.jasteambot.socket.ServerSocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.util.logging.DebugLog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class JaSteamServer implements Runnable {

    // Constant path
    public static final Path CONFIG_FILE = IOHelper.WORKING_DIR.resolve("JaSteam.cfg");
    public static final Path SENTRY_DIR = IOHelper.WORKING_DIR.resolve("sentryfiles");
    public static final Path AUTH_DIR = IOHelper.WORKING_DIR.resolve("authfiles");
    public static final Path KEY_DIR = IOHelper.WORKING_DIR.resolve("loginkeys");

    private static final Logger LOGGER = LoggerFactory.getLogger(JaSteamServer.class);
    // Handler
    public static CommandHandler COMMAND_HANDLER;
    // Server configuration
    private final Config config;
    private final ServerSocketHandler serverSocketHandler;

    // Bot manager
    private final BotManager botManager;

    private JaSteamServer() throws IOException {
        if (Boolean.getBoolean("jasteam.debug")) {
            LogManager.getInstance().setLoggingLevel(Level.DEBUG);
        }

        // Generate config
        generateConfigIfNotExists();
        LOGGER.info("Reading JaSteamBot config file");
        try (BufferedReader reader = IOHelper.newReader(CONFIG_FILE)) {
            config = new Config(TextConfigReader.read(reader, true));
        }
        config.verify();

        // Set server socket thread
        serverSocketHandler = new ServerSocketHandler(this);

        // Initialize Bot manager
        botManager = new BotManager(config.getBots());

        // Create important dirs
        IOHelper.createDir(SENTRY_DIR);
        IOHelper.createDir(AUTH_DIR);
        IOHelper.createDir(KEY_DIR);

        // SteamKit debug
        DebugLog.addListener(new SteamKitDebugListener());

        // Set command handler
        CommandHandler localCommandHandler;
        try {
            Class.forName("jline.Terminal");

            // JLine2 available
            localCommandHandler = new JLineCommandHandler(this);
            LOGGER.info("JLine2 terminal enabled");
        } catch (ClassNotFoundException ignored) {
            localCommandHandler = new StdCommandHandler(this);
            LOGGER.warn("JLine2 isn't in classpath, using std");
        }
        COMMAND_HANDLER = localCommandHandler;

        if (config.isLaunchAllBots()) {
            botManager.startAll();
        }
    }

    public static void main(String[] args) {
        // Start server
        try {
            new JaSteamServer().run();
        } catch (Exception e) {
            LOGGER.error("Can't start application", e);
        }
    }

    @Override
    public void run() {
        // Add shutdown hook, then start JaSteamBot
        Runtime.getRuntime().addShutdownHook(CommonHelper.newThread(null, false, this::shutdownHook));
        CommonHelper.newThread("ConsoleCommand Thread", true, COMMAND_HANDLER).start();
        rebindServerSocket();
    }

    public void rebindServerSocket() {
        serverSocketHandler.close();
        CommonHelper.newThread("Server Socket Thread", false, serverSocketHandler).start();
    }

    private void generateConfigIfNotExists() throws IOException {
        if (IOHelper.isFile(CONFIG_FILE)) {
            return;
        }

        // Create new config
        Config newConfig;
        LOGGER.info("Creating JaSteamBot config");
        try (BufferedReader reader = IOHelper.newReader(IOHelper.getResourceURL("jasteambot/default/config.cfg"))) {
            newConfig = new Config(TextConfigReader.read(reader, false));
        }

        // Write JaSteamBot config
        LOGGER.info("Writing JaSteamBot config file");
        try (BufferedWriter writer = IOHelper.newWriter(CONFIG_FILE)) {
            TextConfigWriter.write(newConfig.block, writer, true);
        }
    }

    private void shutdownHook() {
        serverSocketHandler.close();
        botManager.close();
        LOGGER.info("JaSteamBot server stopped");
    }

    public Config getConfig() {
        return config;
    }

    public CommandHandler getCommandHandler() {
        return COMMAND_HANDLER;
    }

    public ServerSocketHandler getServerSocketHandler() {
        return serverSocketHandler;
    }

    public BotManager getBotManager() {
        return botManager;
    }

    public static final class Config extends ConfigObject {
        private final StringConfigEntry address;
        private final IntegerConfigEntry port;
        private final StringConfigEntry jmsAddress;
        private final IntegerConfigEntry jmsPort;
        private final StringConfigEntry jmsLogin;
        private final StringConfigEntry jmsPassword;
        private final StringConfigEntry apikey;
        private final List<Long> admins;
        private final BooleanConfigEntry launchAllBots;
        private final Map<Integer, BotConfig> bots;

        private Config(BlockConfigEntry block) {
            super(block);
            this.address = block.getEntry("address", StringConfigEntry.class);
            this.port = block.getEntry("port", IntegerConfigEntry.class);
            this.jmsAddress = block.getEntry("jmsAddress", StringConfigEntry.class);
            this.jmsPort = block.getEntry("jmsPort", IntegerConfigEntry.class);
            this.jmsLogin = block.getEntry("jmsLogin", StringConfigEntry.class);
            this.jmsPassword = block.getEntry("jmsPassword", StringConfigEntry.class);
            this.apikey = block.getEntry("apikey", StringConfigEntry.class);
            this.launchAllBots = block.getEntry("launchAllBots", BooleanConfigEntry.class);
            this.admins = Arrays.asList(block.getEntry("admins", ListConfigEntry.class).
                    stream(LongConfigEntry.class).toArray(Long[]::new));

            // Read all bots
            ListConfigEntry botList = block.getEntry("bots", ListConfigEntry.class);
            Map<Integer, BotConfig> botDataMap = new HashMap<>();
            botList.stream(BlockConfigEntry.class).forEach(bot -> {
                Integer id = ((IntegerConfigEntry) VerifyHelper.getMapValue(bot, "id", "Can't get bot id")).getValue();
                String username = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "username", "Can't get bot username")).getValue();
                String password = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "password", "Can't get bot password")).getValue();
                String apiKey = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "apiKey", "Can't get bot apiKey")).getValue();
                String displayNamePrefix = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "displaynamePrefix", "Can't get bot displaynamePrefix")).getValue();
                String displayname = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "displayname", "Can't get bot displayname")).getValue();
                String chatResponse = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "chatResponse", "Can't get bot chatResponse")).getValue();
                Integer maximumTradeTime = ((IntegerConfigEntry) VerifyHelper.getMapValue(bot, "maximumTradeTime", "Can't get bot maximumTradeTime")).getValue();
                Integer maximumActionGap = ((IntegerConfigEntry) VerifyHelper.getMapValue(bot, "maximumActionGap", "Can't get bot maximumActionGap")).getValue();
                Integer tradePollingInterval = ((IntegerConfigEntry) VerifyHelper.getMapValue(bot, "tradePollingInterval", "Can't get bot tradePollingInterval")).getValue();
                Integer tradeOfferPoolingIntervalSecs = ((IntegerConfigEntry) VerifyHelper.getMapValue(bot, "tradeOfferPoolingIntervalSecs", "Can't get bot tradeOfferPoolingIntervalSecs")).getValue();
                String logFile = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "logFile", "Can't get bot logFile")).getValue();
                String botControlClass = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "botControlClass", "Can't get bot botControlClass")).getValue();
                String type = ((StringConfigEntry) VerifyHelper.getMapValue(bot, "type", "Can't get bot type")).getValue();


                BotConfig botConfig = BotConfig.newBuilder().setId(id)
                        .setUsername(username)
                        .setPassword(password)
                        .setApikey(apiKey)
                        .setDisplaynameprefix(displayNamePrefix)
                        .setDisplayname(displayname)
                        .setChatResponse(chatResponse)
                        .setMaxTradeTime(maximumTradeTime)
                        .setMaxActionGap(maximumActionGap)
                        .setTradePoolingInterval(tradePollingInterval)
                        .setTradeOfferPoolingIntervalSecs(tradeOfferPoolingIntervalSecs)
                        .setLogFile(logFile)
                        .setBotControlClass(botControlClass)
                        .setType(type)
                        .setAdmins(admins)
                        .build();

                VerifyHelper.putIfAbsent(botDataMap, id, botConfig, "Bot with %s id already exists");
            });

            bots = Collections.unmodifiableMap(botDataMap);
        }

        public String getAddress() {
            return address.getValue();
        }

        public int getPort() {
            return port.getValue();
        }

        public String getJmsAddress() {
            return jmsAddress.getValue();
        }

        public Integer getJmsPort() {
            return jmsPort.getValue();
        }

        public String getJmsLogin() {
            return jmsLogin.getValue();
        }

        public String getJmsPassword() {
            return jmsPassword.getValue();
        }

        public String getApi() {
            return apikey.getValue();
        }

        public List<Long> getAdmins() {
            return admins;
        }

        public boolean isLaunchAllBots() {
            return launchAllBots.getValue();
        }

        public Map<Integer, BotConfig> getBots() {
            return bots;
        }

        public void verify() {
            VerifyHelper.verify(getAddress(), VerifyHelper.NOT_EMPTY, "JaSteamBot address can't be empty");
            VerifyHelper.verify(getJmsAddress(), VerifyHelper.NOT_EMPTY, "JaSteamBot jmsAdress can't be empty");
            VerifyHelper.verify(getJmsLogin(), VerifyHelper.NOT_EMPTY, "JaSteamBot jmsLogin can't be empty");
            VerifyHelper.verify(getJmsPassword(), VerifyHelper.NOT_EMPTY, "JaSteamBot jmsPassword can't be empty");
            VerifyHelper.verify(getApi(), VerifyHelper.NOT_EMPTY, "JaSteamBot apikey can't be empty");
            VerifyHelper.verify(getBots(), VerifyHelper.NOT_EMPTY_MAP, "JaSteamBot bots can't be empty");
        }
    }
}
