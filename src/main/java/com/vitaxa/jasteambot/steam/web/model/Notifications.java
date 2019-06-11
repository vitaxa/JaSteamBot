package com.vitaxa.jasteambot.steam.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName("notifications")
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
public class Notifications {

    @JsonProperty("1")
    private int trades;

    @JsonProperty("2")
    private int gameTurns;

    @JsonProperty("3")
    private int moderatorMessages;

    @JsonProperty("4")
    private int comments;

    @JsonProperty("5")
    private int items;

    @JsonProperty("6")
    private int invites;

    @JsonProperty("8")
    private int gifts;

    @JsonProperty("9")
    private int chat;

    @JsonProperty("10")
    private int helpRequestReplies;

    @JsonProperty("11")
    private int accountAlerts;

    public int getTrades() {
        return trades;
    }

    public void setTrades(int trades) {
        this.trades = trades;
    }

    public int getGameTurns() {
        return gameTurns;
    }

    public void setGameTurns(int gameTurns) {
        this.gameTurns = gameTurns;
    }

    public int getModeratorMessages() {
        return moderatorMessages;
    }

    public void setModeratorMessages(int moderatorMessages) {
        this.moderatorMessages = moderatorMessages;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getItems() {
        return items;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public int getInvites() {
        return invites;
    }

    public void setInvites(int invites) {
        this.invites = invites;
    }

    public int getGifts() {
        return gifts;
    }

    public void setGifts(int gifts) {
        this.gifts = gifts;
    }

    public int getChat() {
        return chat;
    }

    public void setChat(int chat) {
        this.chat = chat;
    }

    public int getHelpRequestReplies() {
        return helpRequestReplies;
    }

    public void setHelpRequestReplies(int helpRequestReplies) {
        this.helpRequestReplies = helpRequestReplies;
    }

    public int getAccountAlerts() {
        return accountAlerts;
    }

    public void setAccountAlerts(int accountAlerts) {
        this.accountAlerts = accountAlerts;
    }

    @Override
    public String toString() {
        return "Notifications{" +
                "trades=" + trades +
                ", gameTurns=" + gameTurns +
                ", moderatorMessages=" + moderatorMessages +
                ", comments=" + comments +
                ", items=" + items +
                ", invites=" + invites +
                ", gifts=" + gifts +
                ", chat=" + chat +
                ", helpRequestReplies=" + helpRequestReplies +
                ", accountAlerts=" + accountAlerts +
                '}';
    }
}
