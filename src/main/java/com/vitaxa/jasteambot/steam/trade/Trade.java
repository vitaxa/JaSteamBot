package com.vitaxa.jasteambot.steam.trade;

import com.vitaxa.jasteambot.helper.VerifyHelper;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.cache.SteamCache;
import com.vitaxa.jasteambot.steam.event.Event;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.inventory.InventoryException;
import com.vitaxa.jasteambot.steam.inventory.exception.TradeException;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import com.vitaxa.jasteambot.steam.trade.webapi.*;
import com.vitaxa.jasteambot.steam.web.SteamWeb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.*;
import java.util.stream.Collectors;

public final class Trade {

    private static final Logger LOG = LoggerFactory.getLogger(Trade.class);

    // list to store all trade events already processed
    private final List<TradeEvent> eventList;

    // current bot's sid
    private final SteamID mySteamId;

    private final Map<Integer, TradeUserAssets> myOfferedItemsLocalCopy;
    private final TradeSession tradeSession;
    private final List<Inventory> myInventoryList;
    private final List<Inventory> otherInventoryList;
    /**
     * When the trade closes, this is called.  It doesn't matter
     * whether or not it was a timeout or an error, this is called
     * to close the trade.
     */
    private final Event<Void> onCloseEvent = new Event<>();
    /**
     * Called when the trade ends awaiting email confirmation.
     */
    private final Event<Long> onAwaitingConfirmationEvent = new Event<>();
    /**
     * This is for handling errors that may occur, like inventories not loading.
     */
    private final Event<String> onErrorEvent = new Event<>();
    /**
     * Specifically for trade_status errors.
     */
    private final Event<TradeStatusType> onStatusErrorEvent = new Event<>();
    /**
     * This occurs after Inventories have been loaded.
     */
    private final Event<Void> onAfterInitEvent = new Event<>();
    /**
     * This occurs when the other user adds an item to the trade.
     */
    private final Event<Item> onUserAddItemEvent = new Event<>();
    /**
     * This occurs when the other user removes an item from the trade.
     */
    private final Event<Item> onUserRemoveItemEvent = new Event<>();
    /**
     * This occurs when the user sends a message to the bot over trade.
     */
    private final Event<String> onMessageEvent = new Event<>();
    /**
     * This occurs when the user sets their ready state to either true of false.
     */
    private final Event<Boolean> onUserSetReadyEvent = new Event<>();
    /**
     * This occurs when the user accepts the trade.
     */
    private final Event<Void> onUserAcceptEvent = new Event<>();
    private Set<TradeUserAssets> myOfferedItems;
    private Set<TradeUserAssets> otherOfferedItems;
    private boolean otherUserTimingOut;
    private SteamID otherSID;
    private boolean tradeCancelledByBot;
    private int numUnknownStatusUpdates;
    private long tradeOfferID; // Used for email confirmation
    private Inventory otherPrivateInventory; // Inventory of the other user
    private boolean tradeStarted = false; // value indicating if a trade has started
    private boolean otherUserCancelled = false; // value indicating if the remote trading partner cancelled the trade
    private boolean otherIsReady = false; // value indicating if the other user is ready to trade
    private boolean otherUserAccepted = false; // value indicating if the remote trading partner accepted the trade
    private boolean meIsReady = false; // value indicating if the bot is ready to trade.
    private boolean hasTradeCompletedOk = false; // value indicating whether the trade completed normally
    private boolean isTradeAwaitingConfirmation = false; // value indicating whether the trade completed awaiting email confirmation


    Trade(SteamID me, SteamID other, SteamWeb steamWeb, Inventory myInventory, Inventory otherInventory) {
        this.mySteamId = Objects.requireNonNull(me, "me can't be null");
        this.otherSID = Objects.requireNonNull(other, "other can't be null");

        this.tradeSession = new TradeSession(steamWeb, other);

        this.eventList = new ArrayList<>();

        this.myOfferedItemsLocalCopy = new HashMap<>();
        this.otherOfferedItems = new HashSet<>();
        this.myOfferedItems = new HashSet<>();

        if (myInventory == null) {
            throw new IllegalArgumentException("myInventory can't be null");
        }

        if (otherInventory == null) {
            throw new IllegalArgumentException("otherInventory can't be null");
        }


        this.otherInventoryList = new ArrayList<>();
        this.otherInventoryList.add(otherInventory);
        this.myInventoryList = new ArrayList<>();
        this.myInventoryList.add(myInventory);
    }

    /**
     * Cancel the trade. This calls the OnClose handler, as well.
     */
    public boolean cancelTrade() {
        tradeCancelledByBot = true;
        return tradeSession.cancelTradeWebCmd();
    }

    /**
     * Adds a specified item by its itemid.
     *
     * @return false if the item was not found in the inventory.
     */
    public boolean addItem(long itemid, GameType gameType) {
        return addItem(new TradeUserAssets(2, itemid, gameType));
    }

    /**
     * Adds a specified item by its itemid.
     *
     * @return false if item was not found in the inventory
     */
    public boolean addItem(long itemid, GameType gameType, long contextid) {
        return addItem(new TradeUserAssets(contextid, itemid, gameType.getAppid()));
    }

    public boolean addItem(TradeUserAssets item) {
        int slot = nextTradeSlot();
        boolean success = tradeSession.addItemWebCmd(item.getAssetid(), slot, item.getAppid(), item.getContextid());

        if (success) {
            myOfferedItemsLocalCopy.put(slot, item);
        }

        return success;
    }

    /**
     * Adds a single item by its defindex.
     *
     * @return true if an item was found
     */
    /*
    public boolean addItemByDefindex(int defindex) throws TradeException {
        // Get bot inventory
        if (myInventory != null) {
            List<Item> items = myInventory.getItemsByDefindex(defindex);
            for (Item item : items) {
                long itemsCheck = myOfferedItemsLocalCopy.values().stream().filter(v -> v.getAssetid() == item.getId()).count();

                if (item != null && itemsCheck <= 0) {
                    return addItem(item.getId());
                }
            }
        }
        return false;
    }

    public int addAllItemsByDefindex(int defindex) throws TradeException {
        return addAllItemsByDefindex(defindex, 0);
    }
    /*

    /**
     * Adds an entire set of items by defindex to each successive slot in the trade.
     *
     * @param defindex The defindex. (ex. 5022 = crates)
     * @param numToAdd The upper limit on amount of items to add. 0 to add all items.
     * @return Number of items added.
     */
    /*
    public int addAllItemsByDefindex(int defindex, int numToAdd) throws TradeException {
        int added = 0;
        if (myInventory != null) {
            List<Item> items = myInventory.getItemsByDefindex(defindex);

            for (Item item : items) {
                long itemsCheck = myOfferedItemsLocalCopy.values().stream().filter(v -> v.getAssetid() == item.getId()).count();

                if (item != null && itemsCheck <= 0 && !item.isNotTradeable()) {
                    boolean success = addItem(item.getId());

                    if (success) added++;

                    if (numToAdd > 0 && added >= numToAdd) {
                        return added;
                    }
                }
            }
        }

        return added;
    }
    */
    public boolean removeItem(TradeUserAssets item) {
        return removeItem(item.getAssetid(), GameType.byNum(item.getAppid()), item.getContextid());
    }

    public boolean removeItem(long itemId, GameType gameType) {
        return removeItem(itemId, gameType, 2);
    }

    public boolean removeItem(long itemId, long appId) {
        return removeItem(itemId, GameType.byNum(appId), 2);
    }


    public boolean removeItem(long itemId, GameType gameType, long contextid) {
        return removeItem(itemId, gameType.getAppid(), contextid);
    }

    /**
     * Removes an item by its itemid.
     *
     * @return false the item was not found in the trade.
     */
    public boolean removeItem(long itemid, long appId, long contextid) {
        int slot = getItemSlot(itemid);

        if (slot <= 0) return false;

        boolean success = tradeSession.removeItemWebCmd(itemid, slot, appId, contextid);

        if (success) {
            myOfferedItemsLocalCopy.remove(slot);
        }

        return success;
    }

    /**
     * Removes an item with the given defindex from the trade.
     *
     * @param defindex
     * @return true if item was found
     */
    /*
    public boolean removeItemByDefindex(int defindex) throws TradeException {
        if (myInventory != null) {
            for (TradeUserAssets asset : myOfferedItemsLocalCopy.values()) {

                Optional<Item> itemOptional = myInventory.getItem(asset.getAssetid());
                if (itemOptional.isPresent()) {
                    Item item = itemOptional.get();
                    if (item.getDefindex() == defindex) {
                        return removeItem(item.getId());
                    }
                }
            }
        }
        return false;
    }

    public int removeAllItemsByDefindex(int defindex) throws TradeException {
        return removeAllItemsByDefindex(defindex, 0);
    }
    /*

    /**
     * Removes an entire set of items by defindex
     *
     * @param defindex    The defindex. (ex. 5022 = crates)
     * @param numToRemove The upper limit on amount of items to remove. 0 to remove all items.
     * @return Number of items removed.
     */
    /*
    public int removeAllItemsByDefindex(int defindex, int numToRemove) throws TradeException {
        int removed = 0;

        if (myInventory != null) {
            List<Item> items = myInventory.getItemsByDefindex(defindex);

            for (Item item : items) {
                long itemsCheck = myOfferedItemsLocalCopy.values().stream().filter(v -> v.getAssetid() == item.getId()).count();

                if (item != null && itemsCheck > 0) {
                    boolean success = removeItem(item.getId());

                    if (success) removed++;

                    if (numToRemove > 0 && removed >= numToRemove) {
                        return removed;
                    }
                }

            }

        }

        return removed;
    }
    */

    /**
     * Removes all offered items from the trade.
     *
     * @return Number of items removed.
     */
    public int removeAllItems() throws InventoryException {
        int numRemoved = 0;

        for (TradeUserAssets asset : myOfferedItemsLocalCopy.values()) {
            final Optional<Item> itemOptional = searchItem(getMyInventoryList(), asset.getAssetid());

            if (itemOptional.isPresent()) {
                Item item = itemOptional.get();

                boolean wasRemoved = removeItem(item.getId(), asset.getAppid());

                if (wasRemoved) numRemoved++;
            }
        }

        return numRemoved;
    }

    /**
     * Sends a message to the user over the trade chat.
     *
     * @return
     */
    public boolean sendMessage(String msg) {
        return tradeSession.sendMessageWebCmd(msg);
    }

    /**
     * Sets the bot to a ready status.
     *
     * @param ready ready or not
     * @return true if successfully set
     */
    public boolean setReady(boolean ready) throws TradeException {
        // If the bot calls setReady(false) and the call fails, we still want meIsReady to be
        // set to false.  Otherwise, if the call to setReady() was a result of a callback
        // from trade.Poll() inside of the OnTradeAccept() handler, the OnTradeAccept()
        // handler might think the bot is ready, when really it's not!
        if (!ready) meIsReady = false;

        validateLocalTradeItems();

        return tradeSession.setReadyWebCmd(ready);
    }

    /**
     * Accepts the trade from the user.  Returns whether the acceptance went through or not
     */
    public boolean acceptTrade() throws TradeException {
        if (!meIsReady) return false;

        validateLocalTradeItems();

        return tradeSession.acceptTradeWebCmd();
    }

    /**
     * This updates the trade.  This is called at an interval of a default of 800ms, not including the execution time
     * of the method itself.
     *
     * @return true if the other trade partner performed an action
     */
    public boolean poll() {
        if (!tradeStarted) {
            tradeStarted = true;

            onAfterInitEvent.handleEvent();
        }

        TradeStatus status = tradeSession.getTradeStatus();

        if (!status.isSuccess()) return false;

        TradeStatusType tradeStatusType = (TradeStatusType) TradeStatusType.byNum((int) status.getTradeStatus());
        switch (tradeStatusType) {
            // Nothing happened. i.e. trade hasn't closed yet.
            case ON_GOING:
                try {
                    return handleTradeOngoing(status);
                } catch (TradeException | InventoryException e) {
                    LOG.error("Handle trade on going exception", e);
                    return false;
                }
            case COMPLETED_SUCCESSFULLY:
                // Successful trade
                hasTradeCompletedOk = true;
                return false;
            case PENDING_CONFIRMATION:
                // Email/mobile confirmation
                isTradeAwaitingConfirmation = true;
                tradeOfferID = Long.parseLong(status.getTradeId());
                return false;
            case EMPTY:
                //On a status of 2, the Steam web code attempts the request two more times
                numUnknownStatusUpdates++;
                if (numUnknownStatusUpdates < 3) {
                    return false;
                }
                break;
        }

        fireOnStatusErrorEvent(tradeStatusType);
        otherUserCancelled = true;

        return false;
    }

    public String getTradeStatusErrorString(TradeStatusType tradeStatusType) {
        switch (tradeStatusType) {
            case ON_GOING:
                return "is still going on";
            case COMPLETED_SUCCESSFULLY:
                return "completed successfully";
            case EMPTY:
                return "completed empty - no items were exchanged";
            case TRADE_CANCELLED:
                return "was cancelled " + (tradeCancelledByBot ? "by bot" : "by other user");
            case SESSION_EXPIRED:
                return String.format("expired because %s timed out", (otherUserTimingOut ? "other user" : "bot"));
            case TRADE_FAILED:
                return "failed unexpectedly";
            case PENDING_CONFIRMATION:
                return "completed - pending confirmation";
            default:
                return "STATUS IS UNKNOWN - THIS SHOULD NEVER HAPPEN!";
        }
    }

    /**
     * Search item in all initialized inventories
     *
     * @param id item id
     * @return return item, null if not found
     * @throws InventoryException if can't read inventory
     */
    private Optional<Item> searchItem(List<Inventory> inventoryList, long id) throws InventoryException {
        for (Inventory inventory : inventoryList) {
            final Optional<Item> itemOptional = inventory.getItem(id);
            if (itemOptional.isPresent()) {
                return itemOptional;
            }
        }

        return Optional.empty();
    }

    /**
     * Gets the initialized inventories of the bot.
     *
     * @return bot inventory
     */
    public List<Inventory> getMyInventoryList() {
        return myInventoryList;
    }

    /**
     * Gets the initialized inventory of the bot.
     *
     * @return bot inventory, null if inventory not initialized
     */
    public Inventory getMyInventory(GameType gameType) {
        final Optional<Inventory> inventoryOptional = myInventoryList.stream()
                .filter(inventory -> inventory.getGameType() == gameType)
                .findFirst();
        return inventoryOptional.orElse(null);
    }

    /**
     * Gets the initialized inventories of the other user.
     *
     * @return other user inventory
     */
    public List<Inventory> getOtherInventoryList() {
        return otherInventoryList;
    }

    public Inventory getOtherInventory(GameType gameType) {
        final Optional<Inventory> inventoryOptional = otherInventoryList.stream()
                .filter(inventory -> inventory.getGameType() == gameType)
                .findFirst();
        return inventoryOptional.orElse(null);
    }

    public Map<Integer, TradeUserAssets> getMyOfferedItemsLocalCopy() {
        return myOfferedItemsLocalCopy;
    }

    public Set<TradeUserAssets> getOtherOfferedItems() {
        return otherOfferedItems;
    }

    public Inventory getOtherPrivateInventory() {
        return otherPrivateInventory;
    }

    public SteamID getOtherSID() {
        return otherSID;
    }

    public SteamID getMySteamId() {
        return mySteamId;
    }

    public boolean isTradeStarted() {
        return tradeStarted;
    }

    public boolean isOtherIsReady() {
        return otherIsReady;
    }

    public boolean isMeIsReady() {
        return meIsReady;
    }

    public boolean hasTradeEnded() {
        return otherUserCancelled || hasTradeCompletedOk || isTradeAwaitingConfirmation || tradeCancelledByBot;
    }

    private Optional<Item> getItemFromPrivateBp(TradeUserAssets asset) throws InventoryException {
        final Inventory inventory = tradeSession.getForeignInventory(otherSID, asset.getContextid(), asset.getAppid());

        otherInventoryList.add(inventory);

        // Cache inventory
        SteamCache.getInstance().saveInventory(otherSID, asset.getAppid(), inventory);

        return inventory.getItem(asset.getAssetid());
    }

    private boolean handleTradeOngoing(TradeStatus status) throws TradeException, InventoryException {
        boolean otherUserDidSomething = false;
        if (status.getNewVersion()) {
            handleTradeVersionChange(status);
            otherUserDidSomething = true;
        } else if (status.getVersion() > tradeSession.getVersion()) {
            // we missed a version update abort so we don't get
            // scammed. if we could get what steam thinks what's in the
            // trade then this wouldn't be an issue. but we can only get
            // that when we see newversion == true
            throw new TradeException("The trade version does not match. Aborting.");
        }

        // Update Local Variables
        if (status.getThem() != null) {
            otherIsReady = status.getThem().getReady() == 1;
            meIsReady = status.getMe().getReady() == 1;
            otherUserAccepted = status.getThem().getConfirmed() == 1;

            //Similar to the logic Steam uses to determine whether or not to show the "waiting" spinner in the trade window
            otherUserTimingOut = (status.getThem().isConnectionPending() || status.getThem().getSecSinceTouch() >= 5);
        }

        List<TradeEvent> eventSorted = status.getAllEvents();
        Collections.sort(eventSorted);

        for (TradeEvent tradeEvent : eventSorted) {
            if (eventList.contains(tradeEvent)) continue;

            //add event to processed list, as we are taking care of this event now
            eventList.add(tradeEvent);

            boolean isBot = tradeEvent.getSteamid().equalsIgnoreCase(String.valueOf(mySteamId.convertToUInt64()));

            // dont process if this is something the bot did
            if (isBot) continue;

            otherUserDidSomething = true;
            switch (TradeEventType.byEventNum(tradeEvent.getAction())) {
                case ITEM_ADDED:
                    LOG.debug("HANDLE TRADE ON GOING: ITEM_ADDED");
                    TradeUserAssets newAsset = new TradeUserAssets(tradeEvent.getContextid(), tradeEvent.getAssetid(), tradeEvent.getAppid());
                    if (!otherOfferedItems.contains(newAsset)) {
                        otherOfferedItems.add(newAsset);
                        fireOnUserAddItem(newAsset);
                    }
                    break;
                case ITEM_REMOVED:
                    LOG.debug("HANDLE TRADE ON GOING: ITEM_REMOVED");
                    TradeUserAssets oldAsset = new TradeUserAssets(tradeEvent.getContextid(), tradeEvent.getAssetid(), tradeEvent.getAppid());
                    if (otherOfferedItems.contains(oldAsset)) {
                        otherOfferedItems.remove(oldAsset);
                        fireOnUserRemoveItem(oldAsset);
                    }
                    break;
                case USER_SET_READY:
                    LOG.debug("HANDLE TRADE ON GOING: USER_SET_READY");
                    onUserSetReadyEvent.handleEvent(true);
                    break;
                case USER_SET_UNREADY:
                    LOG.debug("HANDLE TRADE ON GOING: USER_SET_UNREADY");
                    onUserSetReadyEvent.handleEvent(false);
                    break;
                case USER_ACCEPT:
                    LOG.debug("HANDLE TRADE ON GOING: USER_ACCEPT");
                    onUserAcceptEvent.handleEvent();
                    break;
                case USER_CHAT:
                    LOG.debug("HANDLE TRADE ON GOING: USER_CHAT");
                    onMessageEvent.handleEvent(tradeEvent.getText());
                    break;
                default:
                    throw new TradeException("Unknown event type: " + tradeEvent.getAction());
            }
        }

        if (status.getLogPos() != 0) {
            tradeSession.getLogPos().set(status.getLogPos());
        }

        return otherUserDidSomething;
    }

    private void handleTradeVersionChange(TradeStatus status) throws TradeException, InventoryException {
        //Figure out which items have been added/removed
        Set<TradeUserAssets> otherOfferedItemsUpdated = status.getThem().getAssets();
        List<TradeUserAssets> addedItems = otherOfferedItemsUpdated.stream().filter(i -> !otherOfferedItems.contains(i))
                .collect(Collectors.toList());
        List<TradeUserAssets> removedItems = otherOfferedItems.stream().filter(i -> !otherOfferedItemsUpdated.contains(i))
                .collect(Collectors.toList());

        //Copy over the new items and update the version number
        otherOfferedItems = status.getThem().getAssets();
        myOfferedItems = status.getMe().getAssets();
        tradeSession.setVersion(status.getVersion());

        //Fire the OnUserRemoveItem events
        for (TradeUserAssets asset : removedItems) {
            fireOnUserRemoveItem(asset);
        }

        //Fire the OnUserAddItem events
        for (TradeUserAssets asset : addedItems) {
            fireOnUserAddItem(asset);
        }
    }

    /**
     * Gets an item from a TradeEvent, and passes it into the UserHandler's implemented OnUserAddItem([...]) routine.
     * Passes in null items if something went wrong.
     */
    private void fireOnUserAddItem(TradeUserAssets asset) throws InventoryException, TradeException {
        if (meIsReady) {
            setReady(false);
        }

        final Optional<Item> itemOptional = searchItem(getOtherInventoryList(), asset.getAssetid());
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            onUserAddItemEvent.handleEvent(item);
        } else {
            final Optional<Item> itemFromPrivateBpOptional = getItemFromPrivateBp(asset);
            itemFromPrivateBpOptional.ifPresent(onUserAddItemEvent::handleEvent);
        }
    }

    /**
     * Gets an item from a TradeEvent, and passes it into the UserHandler's implemented OnUserRemoveItem([...]) routine.
     * Passes in null items if something went wrong.
     */
    private void fireOnUserRemoveItem(TradeUserAssets asset) throws TradeException, InventoryException {
        if (meIsReady) {
            setReady(false);
        }

        final Optional<Item> itemOptional = searchItem(getOtherInventoryList(), asset.getAssetid());
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            onUserRemoveItemEvent.handleEvent(item);
        } else {
            final Optional<Item> itemFromPrivateBpOptional = getItemFromPrivateBp(asset);
            itemFromPrivateBpOptional.ifPresent(onUserRemoveItemEvent::handleEvent);
        }
    }

    protected void fireOnAwaitingConfirmation() {
        onAwaitingConfirmationEvent.handleEvent(tradeOfferID);
    }

    protected void fireOnCloseEvent() {
        onCloseEvent.handleEvent();
    }

    protected void fireOnErrorEvent(String message) {
        onErrorEvent.handleEvent(message);
    }

    private void fireOnStatusErrorEvent(TradeStatusType statusType) {
        onStatusErrorEvent.handleEvent(statusType);
    }

    private int nextTradeSlot() {
        int slot = 0;
        while (myOfferedItemsLocalCopy.containsKey(slot)) {
            slot++;
        }
        return slot;
    }

    private int getItemSlot(long itemid) {
        for (Map.Entry<Integer, TradeUserAssets> entry : myOfferedItemsLocalCopy.entrySet()) {
            if (entry.getValue().getAssetid() == itemid) {
                return entry.getKey();
            }
        }
        return 0;
    }

    private void validateLocalTradeItems() throws TradeException {
        final Set<TradeUserAssets> localItems = new HashSet<>(myOfferedItemsLocalCopy.values());
        final Set<TradeUserAssets> offeredItems = new HashSet<>(myOfferedItems);
        if (!localItems.equals(offeredItems)) {
            throw new TradeException("Error validating local copy of offered items in the trade");
        }
    }

    public boolean isTradeAwaitingConfirmation() {
        return isTradeAwaitingConfirmation;
    }

    public boolean isOtherUserAccepted() {
        return otherUserAccepted;
    }

    public Event<Void> getOnCloseEvent() {
        return onCloseEvent;
    }

    public Event<Long> getOnAwaitingConfirmationEvent() {
        return onAwaitingConfirmationEvent;
    }

    public Event<String> getOnErrorEvent() {
        return onErrorEvent;
    }

    public Event<TradeStatusType> getOnStatusErrorEvent() {
        return onStatusErrorEvent;
    }

    public Event<Void> getOnAfterInitEvent() {
        return onAfterInitEvent;
    }

    public Event<Item> getOnUserAddItemEvent() {
        return onUserAddItemEvent;
    }

    public Event<Item> getOnUserRemoveItemEvent() {
        return onUserRemoveItemEvent;
    }

    public Event<String> getOnMessageEvent() {
        return onMessageEvent;
    }

    public Event<Boolean> getOnUserSetReadyEvent() {
        return onUserSetReadyEvent;
    }

    public Event<Void> getOnUserAcceptEvent() {
        return onUserAcceptEvent;
    }

    public enum TradeStatusType {
        ON_GOING(0),
        COMPLETED_SUCCESSFULLY(1),
        EMPTY(2),
        TRADE_CANCELLED(3),
        SESSION_EXPIRED(4),
        TRADE_FAILED(5),
        PENDING_CONFIRMATION(6);

        private final static Map<Integer, TradeStatusType> TYPES;

        static {
            TradeStatusType[] tradeStatusTypes = values();
            TYPES = new HashMap<>(tradeStatusTypes.length);
            for (TradeStatusType confMethod : tradeStatusTypes) {
                TYPES.put(confMethod.status, confMethod);
            }
        }

        private final int status;

        TradeStatusType(int status) {
            this.status = status;
        }

        public static TradeStatusType byNum(int num) {
            return VerifyHelper.getMapValue(TYPES, num, String.format("Unknown trade status num %s", num));
        }
    }
}
