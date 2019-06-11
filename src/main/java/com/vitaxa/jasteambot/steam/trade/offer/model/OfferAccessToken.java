package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OfferAccessToken {
    @JsonProperty("trade_offer_access_token")
    private String tradeOfferAccessToken;

    public OfferAccessToken(String tradeOfferAccessToken) {
        this.tradeOfferAccessToken = tradeOfferAccessToken;
    }

    public String getTradeOfferAccessToken() {
        return tradeOfferAccessToken;
    }
}
