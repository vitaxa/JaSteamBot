package com.vitaxa.jasteambot.steam.web.model;

public class TradeUrl {

    private final String url;

    private final String token;

    public TradeUrl(String url, String token) {
        this.url = url;
        this.token = token;
    }

    public String getUrl() {
        return url;
    }

    public String getToken() {
        return token;
    }
}
