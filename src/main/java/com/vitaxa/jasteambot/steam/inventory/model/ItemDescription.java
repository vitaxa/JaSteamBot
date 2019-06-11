package com.vitaxa.jasteambot.steam.inventory.model;

import java.time.LocalDateTime;

public class ItemDescription {

    private final long appId;

    private final String name;

    private final String marketName;

    private final String marketHashName;

    private final boolean tradable;

    private final boolean marketable;

    private final Integer marketTradableRestriction;

    private final LocalDateTime cacheExpiration;

    private final String fraudWarnings;

    public ItemDescription(Builder builder) {
        this.appId = builder.getAppId();
        this.name = builder.getName();
        this.marketName = builder.getMarketName();
        this.marketHashName = builder.getMarketHashName();
        this.tradable = builder.isTradable();
        this.marketable = builder.isMarketable();
        this.marketTradableRestriction = builder.getMarketTradableRestriction();
        this.cacheExpiration = builder.getCacheExpiration();
        this.fraudWarnings = builder.getFraudwarnings();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public long getAppId() {
        return appId;
    }

    public String getName() {
        return name;
    }

    public String getMarketName() {
        return marketName;
    }

    public String getMarketHashName() {
        return marketHashName;
    }

    public boolean isTradable() {
        return tradable;
    }

    public boolean isMarketable() {
        return marketable;
    }

    public Integer getMarketTradableRestriction() {
        return marketTradableRestriction;
    }

    public LocalDateTime getCacheExpiration() {
        return cacheExpiration;
    }

    public String getFraudWarnings() {
        return fraudWarnings;
    }

    @Override
    public String toString() {
        return "ItemDescription{" +
                "appId=" + appId +
                ", name='" + name + '\'' +
                ", marketName='" + marketName + '\'' +
                ", marketHashName='" + marketHashName + '\'' +
                ", tradable=" + tradable +
                ", marketable=" + marketable +
                ", marketTradableRestriction=" + marketTradableRestriction +
                ", cacheExpiration=" + cacheExpiration +
                ", fraudWarnings='" + fraudWarnings + '\'' +
                '}';
    }

    public final static class Builder {

        private long appId;

        private String name;

        private String marketName;

        private String marketHashName;

        private boolean tradable;

        private boolean marketable;

        private Integer marketTradableRestriction;

        private LocalDateTime cacheExpiration;

        private String fraudwarnings;

        private Builder() {
        }

        public long getAppId() {
            return appId;
        }

        public Builder setAppId(long appId) {
            this.appId = appId;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getMarketName() {
            return marketName;
        }

        public Builder setMarketName(String marketName) {
            this.marketName = marketName;
            return this;
        }

        public String getMarketHashName() {
            return marketHashName;
        }

        public Builder setMarketHashName(String marketHashName) {
            this.marketHashName = marketHashName;
            return this;
        }

        public boolean isTradable() {
            return tradable;
        }

        public Builder setTradable(boolean tradable) {
            this.tradable = tradable;
            return this;
        }

        public boolean isMarketable() {
            return marketable;
        }

        public Builder setMarketable(boolean marketable) {
            this.marketable = marketable;
            return this;
        }

        public Integer getMarketTradableRestriction() {
            return marketTradableRestriction;
        }

        public Builder setMarketTradableRestriction(Integer marketTradableRestriction) {
            this.marketTradableRestriction = marketTradableRestriction;
            return this;
        }

        public LocalDateTime getCacheExpiration() {
            return cacheExpiration;
        }

        public Builder setCacheExpiration(LocalDateTime cacheExpiration) {
            this.cacheExpiration = cacheExpiration;
            return this;
        }

        public String getFraudwarnings() {
            return fraudwarnings;
        }

        public Builder setFraudwarnings(String fraudwarnings) {
            this.fraudwarnings = fraudwarnings;
            return this;
        }

        public ItemDescription build() {
            return new ItemDescription(this);
        }
    }

}
