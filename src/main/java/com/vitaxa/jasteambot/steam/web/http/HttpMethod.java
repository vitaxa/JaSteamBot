package com.vitaxa.jasteambot.steam.web.http;

public enum HttpMethod {
    GET("GET"), POST("POST"), HEAD("HEAD");
    public final String method;

    HttpMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return method;
    }
}
