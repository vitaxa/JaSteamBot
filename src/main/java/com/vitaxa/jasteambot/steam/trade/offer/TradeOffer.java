package com.vitaxa.jasteambot.steam.trade.offer;

import com.vitaxa.jasteambot.steam.trade.offer.enums.TradeOfferState;
import com.vitaxa.jasteambot.steam.trade.offer.model.CEconAsset;
import com.vitaxa.jasteambot.steam.trade.offer.model.Offer;
import com.vitaxa.jasteambot.steam.trade.offer.model.TradeOfferAcceptResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.base.generated.enums.EAccountType;
import uk.co.thomasc.steamkit.base.generated.enums.EUniverse;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class TradeOffer {

    private static final Logger LOG = LoggerFactory.getLogger(TradeOffer.class);

    private final TradeAssetsState items;
    private final OfferSession session;
    private final SteamID partnerSteamId;
    private final boolean isOurOffer;
    private String tradeOfferId;
    private TradeOfferState offerState;
    private int timeCreated;

    private int expirationTime;

    private int timeUpdated;

    private String message;

    /**
     * This constructor only for our offer
     *
     * @param session          Intended for communicating with steam
     * @param tradeAssetsState our/their items
     * @param partnerSteamId   The steamID of user who will get the trade offer
     */
    protected TradeOffer(OfferSession session, TradeAssetsState tradeAssetsState, SteamID partnerSteamId) {
        this.items = Objects.requireNonNull(tradeAssetsState, "tradeAssetsState can't be null");
        this.isOurOffer = true;
        this.offerState = TradeOfferState.UNKNOWN;
        this.session = Objects.requireNonNull(session, "session can't be null");
        this.partnerSteamId = Objects.requireNonNull(partnerSteamId, "partnerSteamId can't be null");
    }

    protected TradeOffer(OfferSession session, Offer offer) {
        List<TradeAsset> myAssets = new ArrayList<>();
        List<TradeAsset> myMissingAssets = new ArrayList<>();
        List<TradeAsset> theirAssets = new ArrayList<>();
        List<TradeAsset> theirMissingAssets = new ArrayList<>();

        if (offer.getItemsToGive() != null) {
            for (CEconAsset asset : offer.getItemsToGive()) {
                TradeAsset tradeAsset = new TradeAsset();
                tradeAsset.createItemAsset(Long.parseLong(asset.getAppId()), Long.parseLong(asset.getContextId()),
                        Long.parseLong(asset.getAssetId()), Long.parseLong(asset.getAmount()));

                if (!asset.isMissing()) {
                    myAssets.add(tradeAsset);
                } else {
                    myMissingAssets.add(tradeAsset);
                }
            }
        }

        if (offer.getItemsToReceive() != null) {
            for (CEconAsset asset : offer.getItemsToReceive()) {
                TradeAsset tradeAsset = new TradeAsset();
                tradeAsset.createItemAsset(Long.parseLong(asset.getAppId()), Long.parseLong(asset.getContextId()),
                        Long.parseLong(asset.getAssetId()), Long.parseLong(asset.getAmount()));
                if (!asset.isMissing()) {
                    theirAssets.add(tradeAsset);
                } else {
                    theirMissingAssets.add(tradeAsset);
                }
            }
        }

        this.session = session;
        this.partnerSteamId = new SteamID(offer.getAccountIdOther(), EUniverse.Public, EAccountType.Individual);
        this.tradeOfferId = offer.getTradeOfferId();
        this.offerState = TradeOfferState.byNum(offer.getTradeOfferState());
        this.isOurOffer = offer.isOurOffer();
        this.expirationTime = offer.getExpirationTime();
        this.timeCreated = offer.getTimeCreated();
        this.timeUpdated = offer.getTimeUpdated();
        this.message = offer.getMessage();
        this.items = new TradeAssetsState(myAssets, theirAssets);
    }

    /**
     * Counter an existing offer with an updated offer
     */
    public String counterOffer(String message) {
        if (tradeOfferId != null && !tradeOfferId.isEmpty()) {
            if (isOurOffer && (offerState == TradeOfferState.ACTIVE && items.isNewVersion())) {
                return session.counterOffer(message, partnerSteamId, items, tradeOfferId);
            }
        }

        throw new IllegalArgumentException("Can't counter offer a trade that doesn't have an offerid, is ours or isn't active");
    }

    /**
     * Send a new trade offer
     */
    public String send() {
        return this.send("");
    }

    /**
     * Send a new trade offer
     *
     * @param message Optional message to included with the trade offer
     */
    public String send(String message) {
        if (tradeOfferId == null) {
            return session.sendTradeOffer(message, partnerSteamId, items);
        }

        throw new IllegalArgumentException("Can't send a trade offer that already exists.");
    }

    public String sendWithToken(String token) {
        return this.sendWithToken(token, "");
    }

    /**
     * Send a new trade offer using a token
     *
     * @param token   The token of the partner
     * @param message Optional message to included with the trade offer
     */
    public String sendWithToken(String token, String message) {
        if (tradeOfferId == null) {
            return session.sendTradeOfferWithToken(message, partnerSteamId, items, token);
        }

        throw new IllegalArgumentException("Can't send a trade offer that already exists.");
    }

    /**
     * Accepts the current offer
     *
     * @return TradeOfferAcceptResponse object containing accept result
     */
    public TradeOfferAcceptResponse accept() {
        if (tradeOfferId == null) {
            return new TradeOfferAcceptResponse("Can't accept a trade without a tradeofferid");
        }
        if (offerState == TradeOfferState.ACTIVE) {
            return session.accept(tradeOfferId);
        }

        return new TradeOfferAcceptResponse("Can't accept a trade that is not active");
    }

    /**
     * Cancel the current offer
     *
     * @return true if successful, otherwise false
     */
    public boolean cancel() {
        if (tradeOfferId == null) {
            LOG.debug("Can't cancel a trade without a tradeofferid");
            throw new IllegalArgumentException("TradeOfferId");
        }
        if (isOurOffer && offerState == TradeOfferState.ACTIVE) {
            return session.cancel(tradeOfferId);
        }
        LOG.debug("Can't cancel a trade that is not active and ours");

        return false;
    }

    public boolean isFirstOffer() {
        return timeCreated == timeUpdated;
    }

    public TradeOfferState getOfferState() {
        return offerState;
    }

    public String getTradeOfferId() {
        return tradeOfferId;
    }

    public SteamID getPartnerSteamId() {
        return partnerSteamId;
    }

    public boolean isOurOffer() {
        return isOurOffer;
    }

    public int getTimeCreated() {
        return timeCreated;
    }
}
