package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class OfferResponse {
    @JsonProperty("offer")
    private Offer offer;

    @JsonProperty("descriptions")
    private List<AssetDescription> descriptions;

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public List<AssetDescription> getDescriptions() {
        return descriptions;
    }

    public void setDescriptions(List<AssetDescription> descriptions) {
        this.descriptions = descriptions;
    }
}
