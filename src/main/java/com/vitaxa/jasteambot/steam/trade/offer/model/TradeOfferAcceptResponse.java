package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeOfferAcceptResponse {
    private boolean accepted;

    @JsonProperty("tradeid")
    private String tradeId;

    @JsonProperty("strError")
    private String tradeError;

    public TradeOfferAcceptResponse() {
        this.tradeId = "";
        this.tradeError = "";
    }

    public TradeOfferAcceptResponse(String tradeError) {
        this.tradeError = tradeError;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public TradeOfferAcceptResponse setAccepted(boolean accepted) {
        this.accepted = accepted;
        return this;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getTradeError() {
        return tradeError;
    }
}
