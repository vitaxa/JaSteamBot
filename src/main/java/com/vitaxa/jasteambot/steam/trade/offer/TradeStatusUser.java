package com.vitaxa.jasteambot.steam.trade.offer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

public class TradeStatusUser {

    @JsonProperty("assets")
    private List<TradeAsset> assets;

    @JsonProperty("currency")
    private List<TradeAsset> currency;

    @JsonProperty("ready")
    private boolean isReady;

    TradeStatusUser() {
        assets = new ArrayList<>();
        currency = new ArrayList<>();
        isReady = false;
    }

    public boolean addItem(TradeAsset asset) {
        if (!assets.contains(asset)) {
            assets.add(asset);
            return true;
        }
        return false;
    }

    public boolean addCurrencyItem(TradeAsset asset) {
        if (!currency.contains(asset)) {
            currency.add(asset);
            return true;
        }
        return false;
    }

    public boolean removeItem(TradeAsset asset) {
        return assets.remove(asset);
    }

    public boolean removeCurrencyItem(TradeAsset asset) {
        return currency.remove(asset);
    }

    public List<TradeAsset> getAssets() {
        return assets;
    }

    public List<TradeAsset> getCurrency() {
        return currency;
    }

    public boolean isReady() {
        return isReady;
    }
}
