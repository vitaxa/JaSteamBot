package com.vitaxa.jasteambot.steam.trade.offer.enums;

import com.vitaxa.jasteambot.helper.VerifyHelper;

import java.util.HashMap;
import java.util.Map;

public enum TradeOfferConfirmationMethod {
    METHOD_INVALID(0), METHOD_EMAIL(1), METHOD_MOBILE_APP(2);
    private final static Map<Integer, TradeOfferConfirmationMethod> CONFORMATIONS;

    static {
        TradeOfferConfirmationMethod[] tradeOfferConfirmationMethods = values();
        CONFORMATIONS = new HashMap<>(tradeOfferConfirmationMethods.length);
        for (TradeOfferConfirmationMethod confMethod : tradeOfferConfirmationMethods) {
            CONFORMATIONS.put(confMethod.confirmation, confMethod);
        }
    }

    public final int confirmation;

    TradeOfferConfirmationMethod(int confirmation) {
        this.confirmation = confirmation;
    }

    public static TradeOfferConfirmationMethod byMethodNum(int num) {
        return VerifyHelper.getMapValue(CONFORMATIONS, num, String.format("Unknown method num %s", num));
    }

}
