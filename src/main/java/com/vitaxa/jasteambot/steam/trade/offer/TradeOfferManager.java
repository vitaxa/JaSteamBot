package com.vitaxa.jasteambot.steam.trade.offer;

import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.steam.event.Event;
import com.vitaxa.jasteambot.steam.trade.offer.enums.TradeOfferState;
import com.vitaxa.jasteambot.steam.trade.offer.model.Offer;
import com.vitaxa.jasteambot.steam.trade.offer.model.OfferResponse;
import com.vitaxa.jasteambot.steam.trade.offer.model.OffersResponse;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class TradeOfferManager {

    private static final Logger LOG = LoggerFactory.getLogger(TradeOfferManager.class);

    private final Map<String, TradeOfferState> knownTradeOffers = new HashMap<>();
    private final OfferSession session;
    private final TradeOfferWebAPI webApi;
    private final Queue<Offer> unhandledTradeOfferUpdates;
    private final Event<TradeOffer> onTradeOfferUpdated = new Event<>();
    private long lastTimeCheckedOffers = 0;

    public TradeOfferManager(String apiKey, SteamWeb steamWeb) {
        if (apiKey == null) throw new IllegalArgumentException("apiKey");

        this.webApi = new TradeOfferWebAPI(apiKey, steamWeb);
        this.session = new OfferSession(webApi, steamWeb);
        this.unhandledTradeOfferUpdates = new LinkedList<>();
    }

    public void enqueueUpdatedOffers() {
        long startTime = CommonHelper.getUnixTimestamp();

        OffersResponse offersResponse = (lastTimeCheckedOffers == 0 ? webApi.getAllTradeOffers()
                : webApi.getAllTradeOffers(String.valueOf(lastTimeCheckedOffers)));

        addTradeOffersToQueue(offersResponse);

        lastTimeCheckedOffers = startTime - 300; // make sure we don't miss any trade offers due to slightly differing clocks
    }

    public boolean handleNextPendingTradeOfferUpdate() {
        Offer nextOffer;
        if (unhandledTradeOfferUpdates.isEmpty()) {
            return false;
        }
        nextOffer = unhandledTradeOfferUpdates.poll();

        return nextOffer != null && handleTradeOfferUpdate(nextOffer);

    }

    public TradeOffer newOffer(SteamID other, TradeAssetsState tradeAssetsState) {
        return new TradeOffer(session, tradeAssetsState, other);
    }

    public TradeOffer getOffer(String offerId) {
        OfferResponse resp = webApi.getTradeOffer(offerId);
        if (resp != null) {
            if (isOfferValid(resp.getOffer())) {
                return new TradeOffer(session, resp.getOffer());
            } else {
                LOG.error("Offer returned from steam api is not valid: %s", resp.getOffer().getTradeOfferId());
            }
        }
        return null;
    }

    private void addTradeOffersToQueue(OffersResponse offers) {
        if (offers == null || offers.getAllOffers() == null) return;

        for (Offer offer : offers.getAllOffers()) {
            LOG.debug("ADD OFFER: {}", offer);
            unhandledTradeOfferUpdates.offer(offer);
        }
    }

    private boolean handleTradeOfferUpdate(Offer offer) {
        if (knownTradeOffers.containsKey(offer.getTradeOfferId()) &&
                knownTradeOffers.get(offer.getTradeOfferId()) == TradeOfferState.byNum(offer.getTradeOfferState())) {
            return false;
        }

        //make sure the api loaded correctly sometimes the items are missing
        if (isOfferValid(offer)) {
            sendOfferToHandler(offer);
        } else {
            OfferResponse resp = webApi.getTradeOffer(offer.getTradeOfferId());
            if (isOfferValid(resp.getOffer())) {
                sendOfferToHandler(resp.getOffer());
            } else {
                LOG.error("Offer returned from steam api is not valid: {}", resp.getOffer().getTradeOfferId());
                return false;
            }
        }

        return true;
    }

    private boolean isOfferValid(Offer offer) {
        boolean hasItemsToGive = offer.getItemsToGive() != null && offer.getItemsToGive().size() != 0;
        boolean hasItemsToReceive = offer.getItemsToReceive() != null && offer.getItemsToReceive().size() != 0;

        return hasItemsToGive || hasItemsToReceive;
    }

    private void sendOfferToHandler(Offer offer) {
        LOG.debug("SEND OFFER TO HANDLER: {}", offer);
        knownTradeOffers.put(offer.getTradeOfferId(), TradeOfferState.byNum(offer.getTradeOfferState()));
        onTradeOfferUpdated.handleEvent(new TradeOffer(session, offer));
    }

    public Event<TradeOffer> getOnTradeOfferUpdated() {
        return onTradeOfferUpdated;
    }
}
