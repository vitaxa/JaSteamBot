package com.vitaxa.jasteambot.steam.trade.offer.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.vitaxa.jasteambot.steam.trade.offer.enums.TradeOfferConfirmationMethod;

import java.util.List;

public class Offer {
    @JsonProperty("tradeofferid")
    private String tradeOfferId;

    @JsonProperty("accountid_other")
    private int accountIdOther;

    @JsonProperty("message")
    private String message;

    @JsonProperty("expiration_time")
    private int expirationTime;

    @JsonProperty("trade_offer_state")
    private int tradeOfferState;

    @JsonProperty("items_to_give")
    private List<CEconAsset> itemsToGive;

    @JsonProperty("items_to_receive")
    private List<CEconAsset> itemsToReceive;

    @JsonProperty("is_our_offer")
    private boolean isOurOffer;

    @JsonProperty("time_created")
    private int timeCreated;

    @JsonProperty("time_updated")
    private int timeUpdated;

    @JsonProperty("from_real_time_trade")
    private boolean fromRealTimeTrade;

    @JsonProperty("escrow_end_date")
    private int escrowEndDate;

    @JsonProperty("confirmation_method")
    private int confirmationMethod;

    private TradeOfferConfirmationMethod getConfirmationMethod() {
        return TradeOfferConfirmationMethod.byMethodNum(confirmationMethod);
    }

    public Offer setConfirmationMethod(int confirmationMethod) {
        this.confirmationMethod = confirmationMethod;
        return this;
    }

    public String getTradeOfferId() {
        return tradeOfferId;
    }

    public Offer setTradeOfferId(String tradeOfferId) {
        this.tradeOfferId = tradeOfferId;
        return this;
    }

    public int getAccountIdOther() {
        return accountIdOther;
    }

    public Offer setAccountIdOther(int accountIdOther) {
        this.accountIdOther = accountIdOther;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public Offer setMessage(String message) {
        this.message = message;
        return this;
    }

    public int getExpirationTime() {
        return expirationTime;
    }

    public Offer setExpirationTime(int expirationTime) {
        this.expirationTime = expirationTime;
        return this;
    }

    public int getTradeOfferState() {
        return tradeOfferState;
    }

    public Offer setTradeOfferState(int tradeOfferState) {
        this.tradeOfferState = tradeOfferState;
        return this;
    }

    public List<CEconAsset> getItemsToGive() {
        return itemsToGive;
    }

    public Offer setItemsToGive(List<CEconAsset> itemsToGive) {
        this.itemsToGive = itemsToGive;
        return this;
    }

    public List<CEconAsset> getItemsToReceive() {
        return itemsToReceive;
    }

    public Offer setItemsToReceive(List<CEconAsset> itemsToReceive) {
        this.itemsToReceive = itemsToReceive;
        return this;
    }

    public boolean isOurOffer() {
        return isOurOffer;
    }

    public Offer setOurOffer(boolean ourOffer) {
        isOurOffer = ourOffer;
        return this;
    }

    public int getTimeCreated() {
        return timeCreated;
    }

    public Offer setTimeCreated(int timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }

    public int getTimeUpdated() {
        return timeUpdated;
    }

    public Offer setTimeUpdated(int timeUpdated) {
        this.timeUpdated = timeUpdated;
        return this;
    }

    public boolean isFromRealTimeTrade() {
        return fromRealTimeTrade;
    }

    public Offer setFromRealTimeTrade(boolean fromRealTimeTrade) {
        this.fromRealTimeTrade = fromRealTimeTrade;
        return this;
    }

    public int getEscrowEndDate() {
        return escrowEndDate;
    }

    public Offer setEscrowEndDate(int escrowEndDate) {
        this.escrowEndDate = escrowEndDate;
        return this;
    }
}
