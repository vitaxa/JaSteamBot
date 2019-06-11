package com.vitaxa.jasteambot.steam.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Match {

    @JsonProperty("match_id")
    private long matchId;

    @JsonProperty("match_seq_num")
    private long matchSeqNumber;

    @JsonProperty("start_time")
    private long startTime;

    @JsonProperty("lobby_type")
    private int lobbyType;

    @JsonProperty("radiant_team_id")
    private int radiantTeamId;

    @JsonProperty("dire_team_id")
    private int direTeamId;

    @JsonProperty("players")
    private List<MatchPlayer> matchPlayers;

    public long getMatchId() {
        return matchId;
    }

    public void setMatchId(long matchId) {
        this.matchId = matchId;
    }

    public long getMatchSeqNumber() {
        return matchSeqNumber;
    }

    public void setMatchSeqNumber(long matchSeqNumber) {
        this.matchSeqNumber = matchSeqNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getLobbyType() {
        return lobbyType;
    }

    public void setLobbyType(int lobbyType) {
        this.lobbyType = lobbyType;
    }

    public int getRadiantTeamId() {
        return radiantTeamId;
    }

    public void setRadiantTeamId(int radiantTeamId) {
        this.radiantTeamId = radiantTeamId;
    }

    public int getDireTeamId() {
        return direTeamId;
    }

    public void setDireTeamId(int direTeamId) {
        this.direTeamId = direTeamId;
    }

    public List<MatchPlayer> getMatchPlayers() {
        return matchPlayers;
    }

    public void setMatchPlayers(List<MatchPlayer> matchPlayers) {
        this.matchPlayers = matchPlayers;
    }
}
