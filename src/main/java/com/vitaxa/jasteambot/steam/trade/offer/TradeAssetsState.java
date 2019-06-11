package com.vitaxa.jasteambot.steam.trade.offer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class TradeAssetsState {

    @JsonProperty("newversion")
    private boolean newVersion;

    @JsonProperty("version")
    private int version;

    @JsonProperty("me")
    private TradeStatusUser myOfferedItems;

    @JsonProperty("them")
    private TradeStatusUser theirOfferedItems;

    public TradeAssetsState() {
        this.myOfferedItems = new TradeStatusUser();
        this.theirOfferedItems = new TradeStatusUser();
        version = 1;
    }

    public TradeAssetsState(List<TradeAsset> myItems, List<TradeAsset> theirItems) {
        this.myOfferedItems = new TradeStatusUser();
        this.theirOfferedItems = new TradeStatusUser();
        version = 1;
        for (TradeAsset asset : myItems) {
            myOfferedItems.addItem(asset);
        }
        for (TradeAsset asset : theirItems) {
            theirOfferedItems.addItem(asset);
        }
    }

    public boolean addMyItem(int appId, long contextId, long assetId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createItemAsset(appId, contextId, assetId, amount != 0 ? amount : 1);

        return shouldUpdate(myOfferedItems.addItem(asset));
    }

    public boolean addTheirItem(int appId, long contextId, long assetId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createItemAsset(appId, contextId, assetId, amount != 0 ? amount : 1);

        return shouldUpdate(theirOfferedItems.addItem(asset));
    }

    public boolean addMyCurrencyItem(int appId, long contextId, long currencyId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createCurrencyAsset(appId, contextId, currencyId, amount);

        return shouldUpdate(myOfferedItems.addCurrencyItem(asset));
    }

    public boolean addTheirCurrencyItem(int appId, long contextId, long currencyId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createCurrencyAsset(appId, contextId, currencyId, amount);

        return shouldUpdate(theirOfferedItems.addCurrencyItem(asset));
    }

    public boolean removeMyItem(int appId, long contextId, long assetId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createItemAsset(appId, contextId, assetId, amount != 0 ? amount : 1);

        return shouldUpdate(myOfferedItems.removeItem(asset));
    }

    public boolean removeTheirItem(int appId, long contextId, long assetId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createItemAsset(appId, contextId, assetId, amount != 0 ? amount : 1);

        return shouldUpdate(theirOfferedItems.removeItem(asset));
    }

    public boolean removeMyCurrencyItem(int appId, long contextId, long currencyId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createCurrencyAsset(appId, contextId, currencyId, amount);

        return shouldUpdate(myOfferedItems.removeCurrencyItem(asset));
    }

    public boolean removeTheirCurrencyItem(int appId, long contextId, long currencyId, long amount) {
        TradeAsset asset = new TradeAsset();
        asset.createCurrencyAsset(appId, contextId, currencyId, amount);

        return shouldUpdate(theirOfferedItems.removeCurrencyItem(asset));
    }

    //checks if version needs to be updated
    private boolean shouldUpdate(boolean check) {
        if (check) {
            newVersion = true;
            version++;
            return true;
        }
        return false;
    }

    public boolean isNewVersion() {
        return newVersion;
    }

    public int getVersion() {
        return version;
    }

    public TradeStatusUser getMyOfferedItems() {
        return myOfferedItems;
    }

    public TradeStatusUser getTheirOfferedItems() {
        return theirOfferedItems;
    }
}

