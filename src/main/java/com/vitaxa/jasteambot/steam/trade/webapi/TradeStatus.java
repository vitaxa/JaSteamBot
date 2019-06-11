package com.vitaxa.jasteambot.steam.trade.webapi;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;

@JsonAutoDetect(fieldVisibility = ANY)
public final class TradeStatus {

    private String error;

    @JsonProperty("newversion")
    private boolean newVersion;

    private boolean success;

    @JsonProperty("tradeid")
    private String tradeId;

    @JsonProperty("trade_status")
    private long tradeStatus;

    private int version;

    private int logPos;

    private TradeUserObj me;

    private TradeUserObj them;

    private List<TradeEvent> events;

    public String getError() {
        return error;
    }

    public boolean getNewVersion() {
        return newVersion;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getTradeId() {
        return tradeId;
    }

    public long getTradeStatus() {
        return tradeStatus;
    }

    public TradeEvent getLastEvent() {
        if (events == null || events.size() == 0) return null;

        return events.get(events.size() - 1);
    }

    public List<TradeEvent> getAllEvents() {
        if (events == null) return Collections.emptyList();

        return events;
    }

    public boolean isNewVersion() {
        return newVersion;
    }

    public int getVersion() {
        return version;
    }

    public int getLogPos() {
        return logPos;
    }

    public TradeUserObj getMe() {
        return me;
    }

    public TradeUserObj getThem() {
        return them;
    }

    public static class TradeUserObj {

        private int ready;

        private int confirmed;

        @JsonProperty("sec_since_touch")
        private int secSinceTouch;

        private boolean connectionPending;

        private JsonNode assets;

        public Set<TradeUserAssets> getAssets() {
            final Set<TradeUserAssets> tradeUserAssets = new HashSet<>();

            // if items were added in trade the type is an array like so:
            // a normal JSON array
            // "assets": [
            //    {
            //        "assetid": "1693638354", <snip>
            //    }
            // ],
            if (assets.isArray()) {
                for (JsonNode asset : assets) {
                    int appid = asset.get("appid").asInt();
                    long contextid = asset.get("contextid").asLong();
                    long assetid = asset.get("assetid").asLong();
                    int amount = asset.get("amount").asInt();
                    tradeUserAssets.add(new TradeUserAssets(contextid, assetid, appid, amount));
                }
            } else if (assets.isObject()) {
                // when items are removed from trade they look like this:
                // a JSON object like a "list"
                // (item in trade slot 1 was removed)
                // "assets": {
                //    "2": {
                //        "assetid": "1745718856", <snip>
                //    },
                //    "3": {
                //        "assetid": "1690644335", <snip>
                //    }
                // },
                assets.elements().forEachRemaining(jsonNode -> {
                    int appid = jsonNode.get("appid").asInt();
                    long contextid = jsonNode.get("contextid").asLong();
                    long assetid = jsonNode.get("assetid").asLong();
                    int amount = jsonNode.get("amount").asInt();
                    tradeUserAssets.add(new TradeUserAssets(contextid, assetid, appid, amount));
                });
            }

            return tradeUserAssets;
        }

        public int getReady() {
            return ready;
        }

        public int getConfirmed() {
            return confirmed;
        }

        public int getSecSinceTouch() {
            return secSinceTouch;
        }

        public boolean isConnectionPending() {
            return connectionPending;
        }
    }
}
