package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeOffersSummary {
    @JsonProperty("pending_received_count")
    private int pendingReceivedCount;

    @JsonProperty("new_received_count")
    private int newReceivedCount;

    @JsonProperty("updated_received_count")
    private int updatedReceivedCount;

    @JsonProperty("historical_received_count")
    private int historicalReceivedCount;

    @JsonProperty("pending_sent_count")
    private int pendingSentCount;

    @JsonProperty("newly_accepted_sent_count")
    private int newlyAcceptedSentCount;

    @JsonProperty("updated_sent_count")
    private int updatedSentCount;

    @JsonProperty("historical_sent_count")
    private int historicalSentCount;

    public int getPendingReceivedCount() {
        return pendingReceivedCount;
    }

    public void setPendingReceivedCount(int pendingReceivedCount) {
        this.pendingReceivedCount = pendingReceivedCount;
    }

    public int getNewReceivedCount() {
        return newReceivedCount;
    }

    public void setNewReceivedCount(int newReceivedCount) {
        this.newReceivedCount = newReceivedCount;
    }

    public int getUpdatedReceivedCount() {
        return updatedReceivedCount;
    }

    public void setUpdatedReceivedCount(int updatedReceivedCount) {
        this.updatedReceivedCount = updatedReceivedCount;
    }

    public int getHistoricalReceivedCount() {
        return historicalReceivedCount;
    }

    public void setHistoricalReceivedCount(int historicalReceivedCount) {
        this.historicalReceivedCount = historicalReceivedCount;
    }

    public int getPendingSentCount() {
        return pendingSentCount;
    }

    public void setPendingSentCount(int pendingSentCount) {
        this.pendingSentCount = pendingSentCount;
    }

    public int getNewlyAcceptedSentCount() {
        return newlyAcceptedSentCount;
    }

    public void setNewlyAcceptedSentCount(int newlyAcceptedSentCount) {
        this.newlyAcceptedSentCount = newlyAcceptedSentCount;
    }

    public int getUpdatedSentCount() {
        return updatedSentCount;
    }

    public void setUpdatedSentCount(int updatedSentCount) {
        this.updatedSentCount = updatedSentCount;
    }

    public int getHistoricalSentCount() {
        return historicalSentCount;
    }

    public void setHistoricalSentCount(int historicalSentCount) {
        this.historicalSentCount = historicalSentCount;
    }
}
