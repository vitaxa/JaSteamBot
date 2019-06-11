package com.vitaxa.jasteambot.steam.trade.offer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class TradeAsset {
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("appid")
    private long appId;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("contextid")
    private long contextId;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("amount")
    private long amount;

    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("assetid")
    private long assetId;

    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    @JsonSerialize(using = ToStringSerializer.class)
    @JsonProperty("currencyid")
    private long currencyId;

    TradeAsset() {
    }

    public void createItemAsset(long appId, long contextId, long assetId, long amount) {
        this.appId = appId;
        this.contextId = contextId;
        this.assetId = assetId;
        this.amount = amount;
        this.currencyId = 0;
    }

    public void createCurrencyAsset(long appId, long contextId, long currencyId, long amount) {
        this.appId = appId;
        this.contextId = contextId;
        this.currencyId = currencyId;
        this.amount = amount;
        this.assetId = 0;
    }

    public long getAppId() {
        return appId;
    }

    public void setAppId(long appId) {
        this.appId = appId;
    }

    public long getContextId() {
        return contextId;
    }

    public void setContextId(long contextId) {
        this.contextId = contextId;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public long getAssetId() {
        return assetId;
    }

    public void setAssetId(long assetId) {
        this.assetId = assetId;
    }

    public long getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(long currencyId) {
        this.currencyId = currencyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TradeAsset that = (TradeAsset) o;

        if (appId != that.appId) return false;
        if (contextId != that.contextId) return false;
        if (amount != that.amount) return false;
        if (assetId != that.assetId) return false;
        return currencyId == that.currencyId;
    }

    @Override
    public int hashCode() {
        int result = (int) (appId ^ (appId >>> 32));
        result = 31 * result + (int) (contextId ^ (contextId >>> 32));
        result = 31 * result + (int) (amount ^ (amount >>> 32));
        result = 31 * result + (int) (assetId ^ (assetId >>> 32));
        result = 31 * result + (int) (currencyId ^ (currencyId >>> 32));
        return result;
    }
}
