package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TradeOfferResponse {
    @JsonProperty("tradeofferid")
    private String tradeOfferId;

    @JsonProperty("strError")
    private String tradeError;

    public String getTradeOfferId() {
        return tradeOfferId;
    }

    public String getTradeError() {
        return tradeError;
    }
}
