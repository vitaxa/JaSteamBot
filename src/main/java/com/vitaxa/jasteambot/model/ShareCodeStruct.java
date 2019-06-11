package com.vitaxa.jasteambot.model;

public class ShareCodeStruct {

    private final long matchId;

    private final long outcomeId;

    private final int token;

    public ShareCodeStruct(long matchId, long outcomeId, int token) {
        this.matchId = matchId;
        this.outcomeId = outcomeId;
        this.token = token;
    }

    public long getMatchId() {
        return matchId;
    }

    public long getOutcomeId() {
        return outcomeId;
    }

    public int getToken() {
        return token;
    }
}
