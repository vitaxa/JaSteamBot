package com.vitaxa.jasteambot;

public enum BotType {

    DOTA("dota"),

    CSGO("csgo"),

    DEFAULT("default");

    private final String name;

    BotType(String name) {
        this.name = name;
    }

    public static BotType byName(String name) {
        for (BotType botType : values()) {
            if (botType.getName().equalsIgnoreCase(name)) {
                return botType;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
