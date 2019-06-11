package com.vitaxa.jasteambot.steam.trade.webapi;

import java.util.Objects;

public class TradeEvent implements Comparable<TradeEvent> {

    private String steamid;

    private int action;

    private long timestamp;

    private int appid;

    private String text;

    private long contextid;

    private long assetid;

    public String getSteamid() {
        return steamid;
    }

    public int getAction() {
        return action;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getAppid() {
        return appid;
    }

    public String getText() {
        return text;
    }

    public long getContextid() {
        return contextid;
    }

    public long getAssetid() {
        return assetid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TradeEvent that = (TradeEvent) o;
        return action == that.action &&
                timestamp == that.timestamp &&
                contextid == that.contextid &&
                assetid == that.assetid &&
                Objects.equals(steamid, that.steamid) &&
                Objects.equals(appid, that.appid) &&
                Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steamid, action, timestamp, appid, text, contextid, assetid);
    }

    @Override
    public int compareTo(TradeEvent o) {
        return (int) (this.timestamp - o.getTimestamp());
    }

}
