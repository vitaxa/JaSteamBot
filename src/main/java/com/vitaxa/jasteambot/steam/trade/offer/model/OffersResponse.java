package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OffersResponse {
    @JsonProperty("trade_offers_sent")
    private List<Offer> tradeOffersSent;

    @JsonProperty("trade_offers_received")
    private List<Offer> tradeOffersReceived;

    @JsonProperty("descriptions")
    private List<AssetDescription> descriptions;

    private List<Offer> allOffers;

    public List<Offer> getTradeOffersSent() {
        return tradeOffersSent;
    }

    public OffersResponse setTradeOffersSent(List<Offer> tradeOffersSent) {
        this.tradeOffersSent = tradeOffersSent;
        return this;
    }

    public List<Offer> getTradeOffersReceived() {
        return tradeOffersReceived;
    }

    public OffersResponse setTradeOffersReceived(List<Offer> tradeOffersReceived) {
        this.tradeOffersReceived = tradeOffersReceived;
        return this;
    }

    public List<AssetDescription> getDescriptions() {
        return descriptions;
    }

    public OffersResponse setDescriptions(List<AssetDescription> descriptions) {
        this.descriptions = descriptions;
        return this;
    }

    public List<Offer> getAllOffers() {
        if (tradeOffersSent == null) {
            return tradeOffersReceived;
        }

        return tradeOffersReceived == null ? tradeOffersSent : Stream.concat(tradeOffersSent.stream(),
                tradeOffersReceived.stream()).collect(Collectors.toList());
    }
}
