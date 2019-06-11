package com.vitaxa.jasteambot.steam.trade.webapi;

import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.helper.VerifyHelper;

import java.util.Map;

public enum TradeEventType {
    ITEM_ADDED(0),
    ITEM_REMOVED(1),
    USER_SET_READY(2),
    USER_SET_UNREADY(3),
    USER_ACCEPT(4),
    MODIFIED_CURRENCY(6),
    USER_CHAT(7);
    private final static Map<Integer, TradeEventType> TRADE_EVENT_TYPE;

    static {
        TradeEventType[] tradeEventTypes = values();
        TRADE_EVENT_TYPE = MapHelper.newHashMapWithExpectedSize(tradeEventTypes.length);
        for (TradeEventType type : tradeEventTypes) {
            TRADE_EVENT_TYPE.put(type.num, type);
        }
    }

    private final int num;

    TradeEventType(int num) {
        this.num = num;
    }

    public static TradeEventType byEventNum(int num) {
        return VerifyHelper.getMapValue(TRADE_EVENT_TYPE, num, String.format("Unknown event num %s", num));
    }
}
