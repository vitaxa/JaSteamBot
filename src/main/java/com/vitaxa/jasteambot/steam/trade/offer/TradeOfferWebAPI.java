package com.vitaxa.jasteambot.steam.trade.offer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.vitaxa.jasteambot.serialize.ApiResponse;
import com.vitaxa.jasteambot.steam.trade.offer.enums.TradeOfferState;
import com.vitaxa.jasteambot.steam.trade.offer.model.OfferResponse;
import com.vitaxa.jasteambot.steam.trade.offer.model.OffersResponse;
import com.vitaxa.jasteambot.steam.trade.offer.model.TradeOffersSummary;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.steamauth.helper.Json;

import java.time.ZonedDateTime;
import java.util.Objects;

public class TradeOfferWebAPI {

    private final static String DEFAULT_HISTORICAL_CUTOFF = String.valueOf(ZonedDateTime.now().minusDays(3).toEpochSecond());

    private static final String BASE_URL = "https://api.steampowered.com/IEconService/%s/%s/%s";

    private final SteamWeb steamWeb;
    private final String apiKey;

    public TradeOfferWebAPI(String apiKey, SteamWeb steamWeb) {
        this.steamWeb = steamWeb;
        this.apiKey = apiKey;

        Objects.requireNonNull(apiKey, "apiKey");
    }

    private static String booleanConverter(boolean value) {
        return value ? "1" : "0";
    }

    public OfferResponse getTradeOffer(String tradeofferid) {
        String options = String.format("?key=%s&tradeofferid=%s&language=%s&get_descriptions=1", apiKey, tradeofferid, "en_us");
        String url = String.format(BASE_URL, "GetTradeOffer", "v1", options);
        String response = steamWeb.fetch(url, new HttpParameters(HttpMethod.GET));

        ApiResponse apiResponse = Json.getInstance().fromJson(response, new TypeReference<ApiResponse<OfferResponse>>() {
        });

        return (OfferResponse) apiResponse.getResponse();
    }

    public TradeOfferState getOfferState(String tradeofferid) {
        OfferResponse resp = getTradeOffer(tradeofferid);
        if (resp != null && resp.getOffer() != null) {
            return TradeOfferState.byNum(resp.getOffer().getTradeOfferState());
        }
        return TradeOfferState.UNKNOWN;
    }

    public OffersResponse getAllTradeOffers() {
        return this.getAllTradeOffers(DEFAULT_HISTORICAL_CUTOFF, "en_us");
    }

    public OffersResponse getAllTradeOffers(String timeHistoricalCutoff) {
        return this.getAllTradeOffers(timeHistoricalCutoff, "en_us");
    }

    public OffersResponse getAllTradeOffers(String timeHistoricalCutoff, String language) {
        return getTradeOffers(true, true, false, true, false, timeHistoricalCutoff, language);
    }

    public OffersResponse getActiveTradeOffers(boolean getSentOffers, boolean getReceivedOffers, boolean getDescriptions) {
        return this.getActiveTradeOffers(getSentOffers, getReceivedOffers, getDescriptions, "en_us");
    }

    public OffersResponse getActiveTradeOffers(boolean getSentOffers, boolean getReceivedOffers, boolean getDescriptions, String language) {
        return getTradeOffers(getSentOffers, getReceivedOffers, getDescriptions, true, false, DEFAULT_HISTORICAL_CUTOFF, language);
    }

    public OffersResponse getTradeOffers(boolean getSentOffers, boolean getReceivedOffers, boolean getDescriptions,
                                         boolean activeOnly, boolean historicalOnly, String timeHistoricalCutoff, String language) {

        if (!getSentOffers && !getReceivedOffers) {
            throw new IllegalArgumentException("getSentOffers and getReceivedOffers can't be both false");
        }

        String options = String.format("?key=%s&get_sent_offers=%s&get_received_offers=%s&get_descriptions=%s&language=%s&active_only=%s&historical_only=%s&time_historical_cutoff=%s",
                apiKey, booleanConverter(getSentOffers), booleanConverter(getReceivedOffers), booleanConverter(getDescriptions),
                language, booleanConverter(activeOnly), booleanConverter(historicalOnly), timeHistoricalCutoff);
        String url = String.format(BASE_URL, "GetTradeOffers", "v1", options);
        String response = steamWeb.fetch(url, new HttpParameters(HttpMethod.GET));

        ApiResponse apiResponse = Json.getInstance().fromJson(response, new TypeReference<ApiResponse<OffersResponse>>() {
        });

        return (OffersResponse) apiResponse.getResponse();
    }

    public TradeOffersSummary getTradeOffersSummary(int timeLastVisit) {
        String options = String.format("?key=%s&time_last_visit=%s", apiKey, timeLastVisit);
        String url = String.format(BASE_URL, "GetTradeOffersSummary", "v1", options);

        String response = steamWeb.fetch(url, new HttpParameters(HttpMethod.GET));

        ApiResponse apiResponse = Json.getInstance().fromJson(response, new TypeReference<ApiResponse<TradeOffersSummary>>() {
        });

        return (TradeOffersSummary) apiResponse.getResponse();
    }

    private boolean declineTradeOffer(long tradeOfferId) {
        String options = String.format("?key=%s&tradeofferid=%s", apiKey, tradeOfferId);
        String url = String.format(BASE_URL, "DeclineTradeOffer", "v1", options);

        String response = steamWeb.fetch(url, new HttpParameters(HttpMethod.POST));

        final JsonNode responseNode = Json.getInstance().nodeFromJson(response);

        return responseNode.get("success").asInt() == 1;
    }

    private boolean cancelTradeOffer(long tradeOfferId) {
        String options = String.format("?key=%s&tradeofferid=%s", apiKey, tradeOfferId);
        String url = String.format(BASE_URL, "CancelTradeOffer", "v1", options);

        String response = steamWeb.fetch(url, new HttpParameters(HttpMethod.POST));

        final JsonNode responseNode = Json.getInstance().nodeFromJson(response);

        return responseNode.get("success").asInt() == 1;
    }

}
