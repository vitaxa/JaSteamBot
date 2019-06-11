package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AssetDescription {
    @JsonProperty("appid")
    private int appId;

    @JsonProperty("classid")
    private String classId;

    @JsonProperty("instanceid")
    private String instanceId;

    @JsonProperty("currency")
    private boolean isCurrency;

    @JsonProperty("background_color")
    private String backgroundColor;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("icon_url_large")
    private String iconUrlLarge;

    @JsonProperty("descriptions")
    private List<Description> descriptions;

    @JsonProperty("tradable")
    private boolean isTradable;

    @JsonProperty("owner_actions")
    private List<OwnerAction> ownerActions;

    @JsonProperty("name")
    private String name;

    @JsonProperty("name_color")
    private String nameColor;

    @JsonProperty("type")
    private String type;

    @JsonProperty("market_name")
    private String marketName;

    @JsonProperty("market_hash_name")
    private String marketHashName;

    public int getAppId() {
        return appId;
    }

    public AssetDescription setAppId(int appId) {
        this.appId = appId;
        return this;
    }

    public String getClassId() {
        return classId;
    }

    public AssetDescription setClassId(String classId) {
        this.classId = classId;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public AssetDescription setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public boolean isCurrency() {
        return isCurrency;
    }

    public AssetDescription setCurrency(boolean currency) {
        isCurrency = currency;
        return this;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public AssetDescription setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public AssetDescription setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
        return this;
    }

    public String getIconUrlLarge() {
        return iconUrlLarge;
    }

    public AssetDescription setIconUrlLarge(String iconUrlLarge) {
        this.iconUrlLarge = iconUrlLarge;
        return this;
    }

    public List<Description> getDescriptions() {
        return descriptions;
    }

    public AssetDescription setDescriptions(List<Description> descriptions) {
        this.descriptions = descriptions;
        return this;
    }

    public boolean isTradable() {
        return isTradable;
    }

    public AssetDescription setTradable(boolean tradable) {
        isTradable = tradable;
        return this;
    }

    public List<OwnerAction> getOwnerActions() {
        return ownerActions;
    }

    public AssetDescription setOwnerActions(List<OwnerAction> ownerActions) {
        this.ownerActions = ownerActions;
        return this;
    }

    public String getName() {
        return name;
    }

    public AssetDescription setName(String name) {
        this.name = name;
        return this;
    }

    public String getNameColor() {
        return nameColor;
    }

    public AssetDescription setNameColor(String nameColor) {
        this.nameColor = nameColor;
        return this;
    }

    public String getType() {
        return type;
    }

    public AssetDescription setType(String type) {
        this.type = type;
        return this;
    }

    public String getMarketName() {
        return marketName;
    }

    public AssetDescription setMarketName(String marketName) {
        this.marketName = marketName;
        return this;
    }

    public String getMarketHashName() {
        return marketHashName;
    }

    public AssetDescription setMarketHashName(String marketHashName) {
        this.marketHashName = marketHashName;
        return this;
    }

    private static final class Description {
        @JsonProperty("type")
        private String type;
        @JsonProperty("value")
        private String value;

        public String getType() {
            return type;
        }

        public Description setType(String type) {
            this.type = type;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Description setValue(String value) {
            this.value = value;
            return this;
        }
    }

    private static final class OwnerAction {
        @JsonProperty("link")
        private String link;
        @JsonProperty("name")
        private String name;

        public String getLink() {
            return link;
        }

        public OwnerAction setLink(String link) {
            this.link = link;
            return this;
        }

        public String getName() {
            return name;
        }

        public OwnerAction setName(String name) {
            this.name = name;
            return this;
        }
    }
}
