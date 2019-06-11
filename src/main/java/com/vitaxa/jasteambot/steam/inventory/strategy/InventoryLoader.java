package com.vitaxa.jasteambot.steam.inventory.strategy;

import com.vitaxa.jasteambot.steam.inventory.Inventory;

@FunctionalInterface
public interface InventoryLoader {
    public Inventory loadInventory();
}
