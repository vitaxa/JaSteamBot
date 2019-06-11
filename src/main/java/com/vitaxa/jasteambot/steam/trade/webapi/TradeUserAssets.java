package com.vitaxa.jasteambot.steam.trade.webapi;

import com.vitaxa.jasteambot.steam.GameType;

import java.util.Objects;

public class TradeUserAssets implements Comparable<TradeUserAssets> {
    // Inventory type
    private long contextid;

    // Item id
    private long assetid;

    private long appid;
    private int amount;

    public TradeUserAssets(long contextid, long assetid, GameType gameType) {
        this(contextid, assetid, gameType.getAppid());
    }

    public TradeUserAssets(long contextid, long assetid, long appid) {
        this(contextid, assetid, appid, 1);
    }

    public TradeUserAssets(long contextid, long assetid, long appid, int amount) {
        this.contextid = contextid;
        this.assetid = assetid;
        this.appid = appid;
        this.amount = amount;
    }

    @Override
    public int compareTo(TradeUserAssets other) {
        if (appid != other.appid)
            return (appid < other.appid ? -1 : 1);
        if (contextid != other.contextid)
            return (contextid < other.contextid ? -1 : 1);
        if (assetid != other.assetid)
            return (assetid < other.assetid ? -1 : 1);
        if (amount != other.amount)
            return (amount < other.amount ? -1 : 1);
        return 0;
    }

    public long getContextid() {
        return contextid;
    }

    public long getAssetid() {
        return assetid;
    }

    public long getAppid() {
        return appid;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeUserAssets that = (TradeUserAssets) o;
        return contextid == that.contextid &&
                assetid == that.assetid &&
                appid == that.appid &&
                amount == that.amount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextid, assetid, appid, amount);
    }

    @Override
    public String toString() {
        return "TradeUserAssets{" +
                "contextid=" + contextid +
                ", assetid=" + assetid +
                ", appid=" + appid +
                ", amount=" + amount +
                '}';
    }
}
