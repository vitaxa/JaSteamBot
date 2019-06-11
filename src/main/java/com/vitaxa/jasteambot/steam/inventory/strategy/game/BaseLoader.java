package com.vitaxa.jasteambot.steam.inventory.strategy.game;

import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.inventory.strategy.InventoryLoader;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.steamauth.helper.Json;
import uk.co.thomasc.steamkit.types.SteamID;

public abstract class BaseLoader implements InventoryLoader {

    private final SteamWeb steamWeb;
    private final String url;

    BaseLoader(SteamWeb steamWeb, SteamID steamID) {
        this.steamWeb = steamWeb;
        this.url = String.format("https://steamcommunity.com/profiles/%s/inventory/json/%s/2?l=english",
                steamID.convertToUInt64(), gameType().getAppid());
    }

    @Override
    public Inventory loadInventory() {
        // Request JSON inventory
        String jsonResponse = steamWeb.fetch(url, new HttpParameters(HttpMethod.GET));

        return Json.getInstance().fromJson(jsonResponse, Inventory.class);
    }

    protected abstract GameType gameType();

}
