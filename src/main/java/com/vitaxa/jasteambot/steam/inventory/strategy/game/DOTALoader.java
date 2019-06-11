package com.vitaxa.jasteambot.steam.inventory.strategy.game;

import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import uk.co.thomasc.steamkit.types.SteamID;

public class DOTALoader extends BaseLoader {

    public DOTALoader(SteamWeb steamWeb, SteamID steamID) {
        super(steamWeb, steamID);
    }

    @Override
    protected GameType gameType() {
        return GameType.DOTA2;
    }
}
