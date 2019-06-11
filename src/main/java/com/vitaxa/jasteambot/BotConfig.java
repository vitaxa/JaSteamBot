package com.vitaxa.jasteambot;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class BotConfig {

    private final int id;

    private final String username;
    private final String password;

    private final String apiKey;
    private final String chatResponse;

    private final String displayName;
    private final String displayNamePrefix;

    private final int maxTradeTime;
    private final int maxActionGap;

    private final int tradePoolingInterval;
    private final int tradeOfferPollingIntervalSecs;

    private final String logFile;

    private final String botControlClass;

    private final BotType type;

    private final List<Long> admins;

    public BotConfig(Builder builder) {
        this.id = builder.id;
        this.username = Objects.requireNonNull(builder.username, "username");
        this.password = Objects.requireNonNull(builder.password, "password");
        this.apiKey = Objects.requireNonNull(builder.apikey, "apiKey");
        this.chatResponse = Objects.requireNonNull(builder.chatResponse, "chatResponse");
        this.displayName = Objects.requireNonNull(builder.displayname, "displayname");
        this.displayNamePrefix = Objects.requireNonNull(builder.displaynameprefix, "displaynameprefix");
        this.maxTradeTime = builder.maxTradeTime;
        this.maxActionGap = builder.maxActionGap;
        this.tradePoolingInterval = builder.tradePoolingInterval;
        this.tradeOfferPollingIntervalSecs = builder.tradeOfferPoolingIntervalSecs;
        this.logFile = Objects.requireNonNull(builder.logFile, "logFile");
        this.botControlClass = Objects.requireNonNull(builder.botControlClass, "botControlClass");
        if (builder.type == null) {
            throw new IllegalArgumentException("type can't be null");
        }
        final BotType botType = BotType.byName(builder.type);
        if (botType == null) {
            throw new RuntimeException("Unknown bot type: " + builder.type);
        }
        this.type = botType;
        this.admins = builder.admins;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getChatResponse() {
        return chatResponse;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDisplayNamePrefix() {
        return displayNamePrefix;
    }

    public int getMaxTradeTime() {
        return maxTradeTime;
    }

    public int getTradePoolingInterval() {
        return tradePoolingInterval;
    }

    public int getTradeOfferPollingIntervalSecs() {
        return tradeOfferPollingIntervalSecs;
    }

    public int getMaxActionGap() {
        return maxActionGap;
    }

    public String getLogFile() {
        return logFile;
    }

    public BotType getType() {
        return type;
    }

    public String getBotControlClass() {
        return botControlClass;
    }

    public List<Long> getAdmins() {
        return admins;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BotConfig botConfig = (BotConfig) o;
        return id == botConfig.id;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id);
    }

    public static class Builder {
        private int id;
        private String username;
        private String password;
        private String apikey;
        private String chatResponse;
        private String displayname;
        private String displaynameprefix;
        private int maxTradeTime;
        private int tradePoolingInterval;
        private int tradeOfferPoolingIntervalSecs;
        private int maxActionGap;
        private String logFile;
        private String botControlClass;
        private String type;
        private List<Long> admins;

        public Builder setId(int id) {
            this.id = id;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setApikey(String apikey) {
            this.apikey = apikey;
            return this;
        }

        public Builder setChatResponse(String chatResponse) {
            this.chatResponse = chatResponse;
            return this;
        }

        public Builder setDisplayname(String displayname) {
            this.displayname = displayname;
            return this;
        }

        public Builder setDisplaynameprefix(String displaynameprefix) {
            this.displaynameprefix = displaynameprefix;
            return this;
        }

        public Builder setMaxTradeTime(int maxTradeTime) {
            this.maxTradeTime = maxTradeTime;
            return this;
        }

        public Builder setTradePoolingInterval(int tradePoolingInterval) {
            this.tradePoolingInterval = tradePoolingInterval;
            return this;
        }

        public Builder setTradeOfferPoolingIntervalSecs(int tradeOfferPoolingIntervalSecs) {
            this.tradeOfferPoolingIntervalSecs = tradeOfferPoolingIntervalSecs;
            return this;
        }

        public Builder setMaxActionGap(int maxActionGap) {
            this.maxActionGap = maxActionGap;
            return this;
        }

        public Builder setLogFile(String logFile) {
            this.logFile = logFile;
            return this;
        }

        public Builder setBotControlClass(String botControlClass) {
            this.botControlClass = botControlClass;
            return this;
        }

        public Builder setType(String type) {
            this.type = type;
            return this;
        }

        public Builder setAdmins(List<Long> admins) {
            this.admins = Collections.unmodifiableList(admins);
            return this;
        }

        public BotConfig build() {
            return new BotConfig(this);
        }
    }
}

