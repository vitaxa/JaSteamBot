package com.vitaxa.jasteambot.steam.web.model;

import com.vitaxa.jasteambot.steam.GameType;

import java.util.Objects;

public class ItemMarketPrice {

    private final Integer currency;

    private final String hashName;

    private final GameType gameType;

    private final String medianPrice;

    private final String lowestPrice;

    public ItemMarketPrice(Integer currency, String hashName, String medianPrice, String lowestPrice, GameType gameType) {
        this.currency = Objects.requireNonNull(currency, "currency can't be null");
        this.hashName = Objects.requireNonNull(hashName, "hashName can't be null");
        this.gameType = Objects.requireNonNull(gameType, "gameType can't be null");
        this.medianPrice = Objects.requireNonNull(medianPrice, "medianPrice can't be null");
        this.lowestPrice = Objects.requireNonNull(lowestPrice, "lowestPrice can't be null");
    }

    public Integer getCurrency() {
        return currency;
    }

    public String getHashName() {
        return hashName;
    }

    public GameType getGameType() {
        return gameType;
    }

    public String getMedianPrice() {
        return medianPrice;
    }

    public String getLowestPrice() {
        return lowestPrice;
    }
}
