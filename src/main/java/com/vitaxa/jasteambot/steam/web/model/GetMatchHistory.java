package com.vitaxa.jasteambot.steam.web.model;

import java.util.List;

public class GetMatchHistory {

    private List<Match> matches;

    public List<Match> getMatches() {
        return matches;
    }

    public void setMatches(List<Match> matches) {
        this.matches = matches;
    }
}
