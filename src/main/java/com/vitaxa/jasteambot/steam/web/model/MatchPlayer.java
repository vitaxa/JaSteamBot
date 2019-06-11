package com.vitaxa.jasteambot.steam.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MatchPlayer {

    @JsonProperty("account_id")
    private long accountId;

    @JsonProperty("player_slot")
    private int playerSlot;

    @JsonProperty("hero_id")
    private int heroId;

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public int getPlayerSlot() {
        return playerSlot;
    }

    public void setPlayerSlot(int playerSlot) {
        this.playerSlot = playerSlot;
    }

    public int getHeroId() {
        return heroId;
    }

    public void setHeroId(int heroId) {
        this.heroId = heroId;
    }
}
