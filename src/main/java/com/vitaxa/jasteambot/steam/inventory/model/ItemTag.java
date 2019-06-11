package com.vitaxa.jasteambot.steam.inventory.model;

public class ItemTag {

    private final String name;

    private final String category;

    public ItemTag(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    @Override
    public String toString() {
        return "ItemTag{" +
                "name='" + name + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}
