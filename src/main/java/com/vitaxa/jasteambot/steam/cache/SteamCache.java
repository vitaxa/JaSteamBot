package com.vitaxa.jasteambot.steam.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.web.model.ItemAssetInfo;
import com.vitaxa.jasteambot.steam.web.model.ItemMarketPrice;
import com.vitaxa.jasteambot.steam.web.model.SteamProfileInfo;
import uk.co.thomasc.steamkit.types.SteamID;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public final class SteamCache {

    private static SteamCache INSTANCE = new SteamCache();

    private final LoadingCache<Long, Inventory> invCache; // key = steamId + appId

    private final LoadingCache<Long, SteamProfileInfo> profileCache; // key = steamId

    private final LoadingCache<Long, ItemAssetInfo> itemAssetInfoCache; // key = classId + appId

    private final LoadingCache<String, ItemMarketPrice> itemMarketPriceCache; // key = itemHashName + "_" + currency + "_" + appId

    private final LoadingCache<String, Object> commonCache;

    private SteamCache() {
        this.invCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> null);
        this.profileCache = Caffeine.newBuilder()
                .expireAfterWrite(10, TimeUnit.MINUTES)
                .build(key -> null);
        this.itemAssetInfoCache = Caffeine.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(key -> null);
        this.itemMarketPriceCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> null);
        this.commonCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(key -> null);
    }

    public static SteamCache getInstance() {
        return INSTANCE;
    }

    public Inventory getInventory(SteamID steamID, long appid) {
        long key = steamID.convertToUInt64() + appid;
        return invCache.get(key);
    }

    public void saveInventory(SteamID steamID, long appid, Inventory inventory) {
        long key = steamID.convertToUInt64() + appid;
        invCache.put(key, inventory);
    }

    public SteamProfileInfo getProfileInfo(long steamId) {
        return profileCache.get(steamId);
    }

    public void saveProfile(long steamId, SteamProfileInfo profileInfo) {
        profileCache.put(steamId, profileInfo);
    }

    public void saveProfile(Map<Long, SteamProfileInfo> profileInfoMap) {
        profileCache.putAll(profileInfoMap);
    }

    public ItemAssetInfo getItemInfo(long classId, int appId) {
        long key = classId + appId;
        return itemAssetInfoCache.get(key);
    }

    public void saveItemInfo(List<ItemAssetInfo> itemAssetInfoList, int appId) {
        for (ItemAssetInfo itemAssetInfo : itemAssetInfoList) {
            saveItemInfo(itemAssetInfo, appId);
        }
    }

    public void saveItemInfo(ItemAssetInfo itemAssetInfo, int appId) {
        final String classId = itemAssetInfo.getClassId();
        long key = Long.parseLong(classId) + appId;
        itemAssetInfoCache.put(key, itemAssetInfo);
    }

    public ItemMarketPrice getItemMarketPrice(int currency, String itemHashName, long appId) {
        String key = itemHashName + "_" + currency + "_" + appId;
        return itemMarketPriceCache.get(key);
    }

    public void saveItemMarketPrice(ItemMarketPrice itemMarketPrice) {
        final long appId = itemMarketPrice.getGameType().getAppid();
        String key = itemMarketPrice.getHashName() + "_" + itemMarketPrice.getCurrency() + "_" + appId;
        itemMarketPriceCache.put(key, itemMarketPrice);
    }

    public Object getCommonInfo(String key) {
        return commonCache.get(key);
    }

    public void saveCommonInfo(String key, Object value) {
        commonCache.put(key, value);
    }
}
