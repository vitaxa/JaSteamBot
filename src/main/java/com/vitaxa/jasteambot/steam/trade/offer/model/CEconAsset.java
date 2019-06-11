package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CEconAsset {
    @JsonProperty("appid")
    private String appId;

    @JsonProperty("contextid")
    private String contextId;

    @JsonProperty("assetid")
    private String assetId;

    @JsonProperty("classid")
    private String classId;

    @JsonProperty("instanceid")
    private String instanceId;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("missing")
    private boolean isMissing;

    public String getAppId() {
        return appId;
    }

    public CEconAsset setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getContextId() {
        return contextId;
    }

    public CEconAsset setContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public String getAssetId() {
        return assetId;
    }

    public CEconAsset setAssetId(String assetId) {
        this.assetId = assetId;
        return this;
    }

    public String getClassId() {
        return classId;
    }

    public CEconAsset setClassId(String classId) {
        this.classId = classId;
        return this;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public CEconAsset setInstanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public String getAmount() {
        return amount;
    }

    public CEconAsset setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public boolean isMissing() {
        return isMissing;
    }

    public CEconAsset setMissing(boolean missing) {
        isMissing = missing;
        return this;
    }
}
