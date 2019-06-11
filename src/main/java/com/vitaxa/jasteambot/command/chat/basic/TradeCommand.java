package com.vitaxa.jasteambot.command.chat.basic;

import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.command.chat.BotCommand;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import uk.co.thomasc.steamkit.base.generated.enums.EChatEntryType;
import uk.co.thomasc.steamkit.types.SteamID;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public final class TradeCommand extends BotCommand {

    public TradeCommand(Bot bot, SteamID otherSID) {
        super(bot, otherSID);
        addChildCommand(new GetItemCommand(bot, otherSID));
        addChildCommand(new GetAllItemCommand(bot, otherSID));
    }

    @Override
    protected void execute(String... args) {
        bot.openTrade(otherSID);
    }

    @Override
    public String commandName() {
        return "trade";
    }

    @Override
    public String getArgsDescription() {
        return "[app id]";
    }

    @Override
    public String getUsageDescription() {
        return "Start trade with bot";
    }

    private boolean checkTrade() {
        if (bot.getCurrentTrade() == null) {
            bot.openTrade(otherSID);
            bot.getSteamFriends().sendChatMessage(otherSID, EChatEntryType.ChatMsg, "Start trade please");
            return false;
        }
        return true;
    }

    private final class GetAllItemCommand extends BotCommand {

        GetAllItemCommand(Bot bot, SteamID otherSID) {
            super(bot, otherSID);
        }

        @Override
        protected void execute(String... args) {
            final List<Item> items = bot.getCurrentTrade().getMyInventory(GameType.CSGO).getItems();
            for (Item item : items) {
                bot.getCurrentTrade().addItem(item.getId(), GameType.CSGO);
            }
        }

        @Override
        protected boolean beforeExecuteCheck(String... args) {
            return checkTrade();
        }

        @Override
        public String commandName() {
            return "getAllItems";
        }

        @Override
        public String getArgsDescription() {
            return null;
        }

        @Override
        public String getUsageDescription() {
            return "Bot give all his items";
        }
    }

    private final class GetItemCommand extends BotCommand {

        GetItemCommand(Bot bot, SteamID otherSID) {
            super(bot, otherSID);
        }

        @Override
        protected void execute(String... args) {
            final long itemId = Long.parseLong(args[0]);
            final List<Item> items = bot.getCurrentTrade().getMyInventory(GameType.CSGO).getItems();
            final Optional<Item> itemOptional = items.stream()
                    .filter(item -> item.getId() == itemId)
                    .findFirst();
            if (itemOptional.isPresent()) {
                final Item foundedItem = itemOptional.get();
                if (!foundedItem.getDescription().isTradable()) {
                    if (foundedItem.getDescription().getCacheExpiration() != null) {
                        final LocalDateTime cacheExpiration = foundedItem.getDescription().getCacheExpiration();
                        bot.getCurrentTrade().sendMessage(String.format("It's not tradable now. You need to wait: %s",
                                CommonHelper.durationBetweenDateTime(LocalDateTime.now(), cacheExpiration)));
                    } else {
                        bot.getCurrentTrade().sendMessage(String.format("It's not tradable item: %s", itemId));
                    }
                    return;
                }
                final boolean addItem = bot.getCurrentTrade().addItem(itemId, GameType.CSGO);
                if (!addItem) {
                    bot.getCurrentTrade().sendMessage(String.format("Can't add this item %s", itemId));
                }
            } else {
                bot.getCurrentTrade().sendMessage(String.format("Item %s doesn't exists", itemId));
            }
        }

        @Override
        protected boolean beforeExecuteCheck(String... args) {
            return checkTrade() && (args != null && args.length != 0);
        }

        @Override
        public String commandName() {
            return "getItem";
        }

        @Override
        public String getArgsDescription() {
            return "[item id]";
        }

        @Override
        public String getUsageDescription() {
            return "Bot give his item by id";
        }
    }
}
