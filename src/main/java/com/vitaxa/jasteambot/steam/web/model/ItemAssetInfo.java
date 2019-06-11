package com.vitaxa.jasteambot.steam.web.model;

import com.vitaxa.jasteambot.steam.inventory.model.ItemTag;

import java.util.List;

public class ItemAssetInfo {

    private final String classId;

    private final String name;

    private final String marketHashName;

    private final boolean tradable;

    private final boolean marketable;

    private final Integer marketTradableRestriction;

    private final String fraudWarnings;

    private final List<ItemTag> tagList;

    public ItemAssetInfo(Builder builder) {
        this.classId = builder.getClassId();
        this.name = builder.getName();
        this.marketHashName = builder.getMarketHashName();
        this.tradable = builder.isTradable();
        this.marketable = builder.isMarketable();
        this.marketTradableRestriction = builder.getMarketTradableRestriction();
        this.fraudWarnings = builder.getFraudWarnings();
        this.tagList = builder.getTagList();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getClassId() {
        return classId;
    }

    public String getName() {
        return name;
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

    public String getFraudWarnings() {
        return fraudWarnings;
    }

    public List<ItemTag> getTagList() {
        return tagList;
    }

    @Override
    public String toString() {
        return "ItemAssetInfo{" +
                "classId='" + classId + '\'' +
                ", name='" + name + '\'' +
                ", marketHashName='" + marketHashName + '\'' +
                ", tradable=" + tradable +
                ", marketable=" + marketable +
                ", marketTradableRestriction=" + marketTradableRestriction +
                ", fraudWarnings='" + fraudWarnings + '\'' +
                ", tagList=" + tagList +
                '}';
    }

    public final static class Builder {

        private String classId;

        private String name;

        private String marketHashName;

        private boolean tradable;

        private boolean marketable;

        private Integer marketTradableRestriction;

        private String fraudWarnings;

        private List<ItemTag> tagList;

        private Builder() {
        }

        public String getClassId() {
            return classId;
        }

        public Builder setClassId(String classId) {
            this.classId = classId;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
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

        public String getFraudWarnings() {
            return fraudWarnings;
        }

        public Builder setFraudWarnings(String fraudWarnings) {
            this.fraudWarnings = fraudWarnings;
            return this;
        }

        public List<ItemTag> getTagList() {
            return tagList;
        }

        public Builder setTagList(List<ItemTag> tagList) {
            this.tagList = tagList;
            return this;
        }

        public ItemAssetInfo build() {
            return new ItemAssetInfo(this);
        }
    }

}
