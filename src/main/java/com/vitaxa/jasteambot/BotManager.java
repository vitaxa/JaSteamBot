package com.vitaxa.jasteambot;

import com.vitaxa.jasteambot.helper.VerifyHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class BotManager implements Closeable {

    private final static Logger LOGGER = LoggerFactory.getLogger(BotManager.class);

    private final Map<Integer, Bot> runningBots;

    private final Map<Integer, BotConfig> allBots;

    BotManager(Map<Integer, BotConfig> allBots) {
        this.runningBots = new ConcurrentHashMap<>();
        this.allBots = Collections.unmodifiableMap(allBots);
    }

    public void startAll() {
        LOGGER.info("Starting all bots");
        allBots.forEach((integer, botConfig) -> startBot(botConfig));
    }

    public void stopAll() {
        LOGGER.info("Shutting down all bot processes.");

        runningBots.forEach((index, bot) -> {
            LOGGER.info("Killing bot {}-{}.", bot.getBotConfig().getUsername(), bot.getBotConfig().getId());
            bot.stop();
        });
        runningBots.clear();
    }

    public void startByIndex(int index) {
        final BotConfig botConfig = VerifyHelper.getMapValue(allBots, index, String.format("Can't find bot by index: %s", index));
        LOGGER.info("Starting bot ({}) at index {}.", botConfig.getUsername(), index);
        startBot(botConfig);
    }

    public void stopByIndex(int index) {
        final Optional<Bot> botOptional = findBotByIndex(index);
        if (botOptional.isPresent()) {
            final Bot bot = botOptional.get();
            LOGGER.info("Killing bot ({}) at index {}.", bot.getBotConfig().getUsername(), index);
            bot.stop();
            runningBots.remove(index);
        }
    }

    public void startByName(String name) {
        allBots.forEach((index, botConfig) -> {
            if (botConfig.getUsername().equalsIgnoreCase(name)) {
                LOGGER.info("Starting bot by name: {}", name);
                startBot(botConfig);
            }
        });
    }

    public void stopByName(String name) {
        final Optional<Bot> botOptional = findBotByName(name);
        if (botOptional.isPresent()) {
            final Bot bot = botOptional.get();
            LOGGER.info("Stop bot by name: {}", name);
            bot.stop();
            runningBots.remove(bot.getBotConfig().getId());
        }
    }

    public void printAll() {
        allBots.forEach((integer, botData) -> {
            final Bot runningBot = runningBots.get(botData.getId());
            if (runningBot != null) {
                LOGGER.info("{} - RUNNING", botData.getUsername());
            } else {
                LOGGER.info("{} - AVAILABLE", botData.getUsername());
            }
        });
    }

    public Optional<Bot> findBotByName(String name) {
        for (Map.Entry<Integer, Bot> entry : runningBots.entrySet()) {
            final Bot bot = entry.getValue();
            final String botUsername = bot.getBotConfig().getUsername();
            if (botUsername.equalsIgnoreCase(name)) {
                return Optional.of(bot);
            }
        }
        return Optional.empty();
    }

    public Optional<Bot> findBotByIndex(int index) {
        return Optional.ofNullable(runningBots.get(index));
    }

    public Map<Integer, Bot> getRunningBots() {
        return Collections.unmodifiableMap(runningBots);
    }

    public Map<Integer, BotConfig> getAllBots() {
        return Collections.unmodifiableMap(allBots);
    }

    private void startBot(BotConfig botConfig) {
        final Bot bot;
        switch (botConfig.getType()) {
            case DOTA:
                bot = new DotaBot(botConfig);
                break;
            case CSGO:
                bot = new CSGOBot(botConfig);
                break;
            case DEFAULT:
                bot = new Bot(botConfig);
                break;
            default:
                bot = new Bot(botConfig);
        }
        final int botId = botConfig.getId();
        VerifyHelper.putIfAbsent(runningBots, botId, bot, String.format("Bot id %s is already running", botId));
        bot.getOnCloseEvent().addEventListener(args -> runningBots.remove(botId, bot));
        bot.start();
    }

    @Override
    public void close() {
        stopAll();
    }
}
