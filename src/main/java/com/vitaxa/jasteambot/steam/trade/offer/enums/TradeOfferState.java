package com.vitaxa.jasteambot.steam.trade.offer.enums;

import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.helper.VerifyHelper;

import java.util.Map;

public enum TradeOfferState {
    INVALID(1),
    ACTIVE(2),
    ACCEPTED(3),
    COUNTERED(4),
    EXPIRED(5),
    CANCELED(6),
    DECLINED(7),
    INVALIDITEMS(8),
    NEEDSCONFIRMATION(9),
    CANCELEDBYSECONDFACTOR(10),
    INESCROW(11),
    UNKNOWN(0);

    private static final Map<Integer, TradeOfferState> TRADE_OFFER_STATE;

    static {
        TradeOfferState[] tradeOfferStates = values();
        TRADE_OFFER_STATE = MapHelper.newHashMapWithExpectedSize(tradeOfferStates.length);
        for (TradeOfferState tradeOfferState : tradeOfferStates) {
            TRADE_OFFER_STATE.put(tradeOfferState.state, tradeOfferState);
        }
    }

    public final int state;

    TradeOfferState(int state) {
        this.state = state;
    }

    public static TradeOfferState byNum(int num) {
        return VerifyHelper.getMapValue(TRADE_OFFER_STATE, num, String.format("Unknown trade offer state: '%s'", String.valueOf(num)));
    }
}
