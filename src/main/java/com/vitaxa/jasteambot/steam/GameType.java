package com.vitaxa.jasteambot.steam;

import com.vitaxa.jasteambot.helper.VerifyHelper;

import java.util.HashMap;
import java.util.Map;

public enum GameType {
    CSGO(730), TF2(440), DOTA2(570), PUBG(578080), ARTIFACT(583950);

    private static final Map<Long, GameType> GAMETYPES;

    static {
        GameType[] gameTypes = values();
        GAMETYPES = new HashMap<>(gameTypes.length);
        for (GameType gameType : gameTypes) {
            GAMETYPES.put(gameType.appid, gameType);
        }
    }

    private final long appid;

    GameType(long appid) {
        this.appid = appid;
    }

    public static GameType byNum(long num) {
        return VerifyHelper.getMapValue(GAMETYPES, num, String.format("Unknown game type: '%s'", String.valueOf(num)));
    }

    public long getAppid() {
        return appid;
    }
}
