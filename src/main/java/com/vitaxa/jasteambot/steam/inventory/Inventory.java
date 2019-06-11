package com.vitaxa.jasteambot.steam.inventory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.cache.SteamCache;
import com.vitaxa.jasteambot.steam.inventory.deserialize.InvNonApiDeserializer;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import com.vitaxa.jasteambot.steam.inventory.strategy.InventoryLoader;
import com.vitaxa.jasteambot.steam.inventory.strategy.game.ArtifactLoader;
import com.vitaxa.jasteambot.steam.inventory.strategy.game.CSGOLoader;
import com.vitaxa.jasteambot.steam.inventory.strategy.game.DOTALoader;
import com.vitaxa.jasteambot.steam.inventory.strategy.game.PUBGLoader;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonDeserialize(using = InvNonApiDeserializer.class)
public final class Inventory {

    private final List<Item> items;
    private final boolean available;
    private final GameType gameType;

    public Inventory(boolean available, List<Item> items, Long appId) {
        this.available = available;
        this.items = Collections.unmodifiableList(Objects.requireNonNull(items, "items can't be null"));
        this.gameType = appId != null ? GameType.byNum(appId) : null;
    }

    public static Inventory fetchInventory(SteamID steamID, SteamWeb steamWeb, GameType gameType) {
        return fetchInventory(steamID, steamWeb, gameType, false);
    }

    public static Inventory fetchInventory(SteamID steamID, SteamWeb steamWeb, GameType gameType, boolean forceUpdate) {
        final SteamCache steamCache = SteamCache.getInstance();

        if (!forceUpdate) {
            // Trying to load inventory from cache
            Inventory inventory = steamCache.getInventory(steamID, gameType.getAppid());

            if (inventory != null) return inventory;
        }

        final InventoryLoader loader;
        switch (gameType) {
            case CSGO:
                loader = new CSGOLoader(steamWeb, steamID);
                break;
            case DOTA2:
                loader = new DOTALoader(steamWeb, steamID);
                break;
            case PUBG:
                loader = new PUBGLoader(steamWeb, steamID);
                break;
            case ARTIFACT:
                loader = new ArtifactLoader(steamWeb, steamID);
            default:
                throw new AssertionError("Unsupported game type");
        }
        final Inventory inventory = loader.loadInventory();
        steamCache.saveInventory(steamID, gameType.getAppid(), inventory);
        return inventory;
    }

    public boolean isAvailable() {
        return available;
    }

    public Optional<Item> getItem(long id) throws InventoryException {
        // Check for Private Inventory
        if (!available) throw new InventoryException("Unable to access inventory");

        return items.stream().filter(item -> item.getId() == id).findFirst();
    }

    public GameType getGameType() {
        return gameType;
    }

    public List<Item> getItems() {
        return items;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "available=" + available +
                ", items=" + items +
                '}';
    }
}
