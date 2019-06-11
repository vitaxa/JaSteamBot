package com.vitaxa.jasteambot.steam.trade.webapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.steamauth.helper.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public final class TradeSession {

    private static final Logger LOG = LoggerFactory.getLogger(TradeSession.class);

    private final SteamWeb steamWeb;
    private final SteamID otherSID;
    private final AtomicLong logPos = new AtomicLong(0L);
    private int version;
    private String sessionIdEsc;
    private String baseTradeURL;

    public TradeSession(SteamWeb steamWeb, SteamID otherSID) {
        this.steamWeb = steamWeb;
        this.otherSID = otherSID;

        init();
    }

    private void init() {
        version = 1;
        sessionIdEsc = URLDecoder.decode(steamWeb.getSessionId(), StandardCharsets.UTF_8);
        baseTradeURL = String.format("https://steamcommunity.com/trade/%s/", otherSID.convertToUInt64());
    }


    /**
     * Gets the trade status
     * This is the main polling method for trading and must be done at a
     * periodic rate (probably around 1 second).
     *
     * @return A deserialized JSON object into TradeStatus object
     */
    public TradeStatus getTradeStatus() {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(3);
        data.put("sessionid", sessionIdEsc);
        data.put("logpos", String.valueOf(logPos.get()));
        data.put("version", String.valueOf(version));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);

        String response = steamWeb.fetch(baseTradeURL + "tradestatus", params);

        return Json.getInstance().fromJson(response, TradeStatus.class);
    }

    /**
     * @param otherId   other client steamid
     * @param contextId Example: 2
     * @param appid     application id. Example: 440
     * @return A dynamic JSON object.
     */
    public Inventory getForeignInventory(SteamID otherId, long contextId, long appid) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(4);
        data.put("sessionid", sessionIdEsc);
        data.put("steamid", String.valueOf(otherId.convertToUInt64()));
        data.put("appid", String.valueOf(appid));
        data.put("contextid", String.valueOf(contextId));

        HttpParameters params = new HttpParameters(data, HttpMethod.GET);
        String response = steamWeb.fetch(baseTradeURL + "foreigninventory", params);

        return Json.getInstance().fromJson(response, Inventory.class);
    }

    /**
     * Sends a message to the user over the trade chat.
     */
    public boolean sendMessageWebCmd(String msg) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(4);
        data.put("sessionid", sessionIdEsc);
        data.put("message", msg);
        data.put("logpos", String.valueOf(logPos.get()));
        data.put("version", String.valueOf(version));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "chat", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    /**
     * Adds a specified item by its itemid. Since each itemid is
     * unique to each item, you'd first have to find the item, or
     * use AddItemByDefindex instead
     *
     * @return false if the item doesn't exist in the Bot's inventory,
     * and returns true if it appears the item was added.
     */
    public boolean addItemWebCmd(long itemid, int slot, long appid, long contextid) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(5);
        data.put("sessionid", sessionIdEsc);
        data.put("appid", "" + appid);
        data.put("contextid", String.valueOf(contextid));
        data.put("itemid", String.valueOf(itemid));
        data.put("slot", String.valueOf(slot));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "additem", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    /**
     * Removes an item by its itemid.  Read AddItem about itemids
     *
     * @return false if the item isn't in the offered items, or
     * true if it appears it succeeded.
     */
    public boolean removeItemWebCmd(long itemid, int slot, long appid, long contextid) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(5);
        data.put("sessionid", sessionIdEsc);
        data.put("appid", "" + appid);
        data.put("contextid", String.valueOf(contextid));
        data.put("itemid", String.valueOf(itemid));
        data.put("slot", String.valueOf(slot));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "removeitem", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    /**
     * Sets the bot to a ready status.
     */
    public boolean setReadyWebCmd(boolean ready) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(3);
        data.put("sessionid", sessionIdEsc);
        data.put("ready", ready ? "true" : "false");
        data.put("version", String.valueOf(version));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "toggleready", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    /**
     * Accepts the trade from the user
     *
     * @return deserialized JSON object
     */
    public boolean acceptTradeWebCmd() {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(2);
        data.put("sessionid", sessionIdEsc);
        data.put("version", String.valueOf(version));

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "confirm", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    /**
     * Cancel the trade
     */
    public boolean cancelTradeWebCmd() {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(1);
        data.put("sessionid", sessionIdEsc);

        HttpParameters params = new HttpParameters(data, HttpMethod.POST);
        String response = steamWeb.fetch(baseTradeURL + "cancel", params);

        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);

        return isSuccess(jsonNode);
    }

    private boolean isSuccess(JsonNode json) {
        if (json == null) return false;
        try {
            JsonNode success = json.get("success");
            JsonNode result = json.get("result");

            boolean successCheck = success != null && success.asText().equalsIgnoreCase("true");
            boolean resultCheck = result != null && result.get("success").asText().equalsIgnoreCase("11");

            return (successCheck || resultCheck);
        } catch (Exception e) {
            return false;
        }
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public AtomicLong getLogPos() {
        return logPos;
    }
}
