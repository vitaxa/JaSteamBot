package com.vitaxa.jasteambot.steam.trade.offer;

import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.steam.trade.offer.enums.TradeOfferState;
import com.vitaxa.jasteambot.steam.trade.offer.model.OfferAccessToken;
import com.vitaxa.jasteambot.steam.trade.offer.model.TradeOfferAcceptResponse;
import com.vitaxa.jasteambot.steam.trade.offer.model.TradeOfferResponse;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.steamauth.helper.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.Map;

public final class OfferSession {

    private static final Logger LOG = LoggerFactory.getLogger(OfferSession.class);

    private final TradeOfferWebAPI webApi;
    private final SteamWeb steamWeb;

    private final String sendUrl = "https://steamcommunity.com/tradeoffer/new/send";

    public OfferSession(TradeOfferWebAPI webApi, SteamWeb steamWeb) {
        this.webApi = webApi;
        this.steamWeb = steamWeb;

        if (steamWeb.getSessionId() == null) {
            throw new IllegalArgumentException("Need to init session id");
        }
    }

    public TradeOfferAcceptResponse accept(String tradeOfferId) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(3);
        data.put("sessionid", steamWeb.getSessionId());
        data.put("serverid", "1");
        data.put("tradeofferid", tradeOfferId);

        String url = String.format("https://steamcommunity.com/tradeoffer/%s/accept", tradeOfferId);
        String referer = String.format("https://steamcommunity.com/tradeoffer/%s/", tradeOfferId);

        String response = steamWeb.fetch(url, new HttpParameters(data, HttpMethod.POST), false, referer);

        if (!response.isEmpty()) {
            TradeOfferAcceptResponse tdAcceptResp = Json.getInstance().fromJson(response, TradeOfferAcceptResponse.class);
            tdAcceptResp.setAccepted(tdAcceptResp.getTradeError().isEmpty());

            return tdAcceptResp;
        }

        // If it didn't work as expected, check the state, maybe it was accepted after all
        TradeOfferState state = webApi.getOfferState(tradeOfferId);
        TradeOfferAcceptResponse tdAcceptResp = new TradeOfferAcceptResponse();
        tdAcceptResp.setAccepted(state == TradeOfferState.ACCEPTED);

        return tdAcceptResp;
    }

    public boolean decline(String tradeOfferId) {
        String url = String.format("https://steamcommunity.com/tradeoffer/%s/decline", tradeOfferId);

        return declineOrCancel(url, tradeOfferId);
    }

    public boolean cancel(String tradeOfferId) {
        String url = String.format("https://steamcommunity.com/tradeoffer/%s/cancel", tradeOfferId);

        return declineOrCancel(url, tradeOfferId);
    }

    /**
     * Creates a new counter offer
     *
     * @param message      A message to include with the trade offer
     * @param otherSteamId The SteamID of the partner we are trading with
     * @param status       The list of items we and they are going to trade
     * @param tradeOfferId he trade offer Id that will be created if successful
     */
    public String counterOffer(String message, SteamID otherSteamId, TradeAssetsState status, String tradeOfferId) {
        if (tradeOfferId == null || tradeOfferId.isEmpty()) {
            throw new IllegalArgumentException("Trade Offer Id must be set for counter offers");
        }

        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(7);
        data.put("sessionid", steamWeb.getSessionId());
        data.put("serverid", "1");
        data.put("partner", String.valueOf(otherSteamId.convertToUInt64()));
        data.put("tradeoffermessage", message);
        data.put("json_tradeoffer", Json.getInstance().toJson(status));
        data.put("tradeofferid_countered", tradeOfferId);
        data.put("trade_offer_create_params", "{}");

        String referer = String.format("https://steamcommunity.com/tradeoffer/%s/", tradeOfferId);

        return request(sendUrl, data, referer);
    }

    /**
     * Creates a new trade offer
     *
     * @param message      A message to include with the trade offer
     * @param otherSteamId The SteamID of the partner we are trading with
     * @param status       The list of items we and they are going to trade
     * @return New trade offer id
     */
    public String sendTradeOffer(String message, SteamID otherSteamId, TradeAssetsState status) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(6);
        data.put("sessionid", steamWeb.getSessionId());
        data.put("serverid", "1");
        data.put("partner", String.valueOf(otherSteamId.convertToUInt64()));
        data.put("tradeoffermessage", message);
        data.put("json_tradeoffer", Json.getInstance().toJson(status));
        data.put("captcha", "");
        data.put("trade_offer_create_params", "{}");

        String referer = String.format("https://steamcommunity.com/tradeoffer/new/?partner=%s",
                otherSteamId.getAccountID());

        return request(sendUrl, data, referer);
    }

    /**
     * Creates a new trade offer with a token
     *
     * @param message      A message to include with the trade offer
     * @param otherSteamId The SteamID of the partner we are trading with
     * @param status       The list of items we and they are going to trade
     * @param token        The token of the partner we are trading with
     * @return New trade offer id
     */
    public String sendTradeOfferWithToken(String message, SteamID otherSteamId, TradeAssetsState status, String token) {
        if (token != null && token.isEmpty()) {
            throw new IllegalArgumentException("Partner trade offer token is missing");
        }

        OfferAccessToken offerToken = new OfferAccessToken(token);

        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(6);
        data.put("sessionid", steamWeb.getSessionId());
        data.put("serverid", "1");
        data.put("partner", String.valueOf(otherSteamId.convertToUInt64()));
        data.put("tradeoffermessage", message);
        data.put("json_tradeoffer", Json.getInstance().toJson(status));
        data.put("trade_offer_create_params", Json.getInstance().toJson(offerToken));

        String referer = String.format("https://steamcommunity.com/tradeoffer/new/?partner=%s&token=%s",
                otherSteamId.getAccountID(), token);

        return request(sendUrl, data, referer);
    }

    private boolean declineOrCancel(String url, String tradeOfferId) {
        Map<String, String> data = MapHelper.newHashMapWithExpectedSize(3);
        data.put("sessionid", steamWeb.getSessionId());
        data.put("serverid", "1");
        data.put("tradeofferid", tradeOfferId);

        String referer = String.format("https://steamcommunity.com/tradeoffer/%s/", tradeOfferId);

        String response = steamWeb.fetch(url, new HttpParameters(data, HttpMethod.POST), false, referer);

        if (!response.isEmpty()) {
            TradeOfferResponse offerResponse = Json.getInstance().fromJson(response, TradeOfferResponse.class);
            return offerResponse.getTradeOfferId() != null && offerResponse.getTradeOfferId().equals(tradeOfferId);
        } else {
            TradeOfferState state = webApi.getOfferState(tradeOfferId);
            return state == TradeOfferState.DECLINED;
        }

    }

    private String request(String url, Map<String, String> data, String referer) {
        String newTradeOfferId = "";

        String resp = steamWeb.fetch(url, new HttpParameters(data, HttpMethod.POST), referer);

        TradeOfferResponse offerResponse = Json.getInstance().fromJson(resp, TradeOfferResponse.class);
        if (offerResponse.getTradeOfferId() != null && !offerResponse.getTradeOfferId().isEmpty()) {
            newTradeOfferId = offerResponse.getTradeOfferId();
        } else {
            LOG.error("Offer response trade error: {}", offerResponse.getTradeError());
        }

        return newTradeOfferId;
    }

}
