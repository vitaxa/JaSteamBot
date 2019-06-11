package com.vitaxa.jasteambot.steam.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.serialize.ApiResponse;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.cache.SteamCache;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.jasteambot.steam.web.model.*;
import com.vitaxa.steamauth.helper.Json;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SteamWebApi {

    private static final String REQ_PLAYER_INFO = "https://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/";

    private static final String REQ_ASSET_INFO = "https://api.steampowered.com/ISteamEconomy/GetAssetClassInfo/v1/";

    private static final String REQ_DOTA2_MATCH_HISTORY = "https://api.steampowered.com/IDOTA2Match_570/GetMatchHistory/V001/";

    private static final SteamCache STEAM_CACHE = SteamCache.getInstance();

    private final Bot bot;

    public SteamWebApi(Bot bot) {
        this.bot = bot;
    }

    private static <T> CacheDataHolder<T> tryGetFromCache(Function<String, T> func, String... searchList) {
        return tryGetFromCache(func, Arrays.asList(searchList));
    }

    private static <T> CacheDataHolder<T> tryGetFromCache(Function<String, T> func, List<String> searchList) {
        final List<T> founded = new ArrayList<>();
        final List<String> needToFound = new ArrayList<>();
        for (String value : searchList) {
            final T cachedVal = func.apply(value);
            if (cachedVal != null) {
                founded.add(cachedVal);
            } else {
                needToFound.add(value);
            }
        }

        return new CacheDataHolder<>(founded, needToFound);
    }

    public List<SteamProfileInfo> getProfileInfo(List<String> steamIds) {
        String[] steamIdArr = new String[steamIds.size()];
        return getProfileInfo(steamIds.toArray(steamIdArr));
    }

    public List<SteamProfileInfo> getProfileInfo(String... steamIds) {
        final CacheDataHolder<SteamProfileInfo> steamProfileInfoCached =
                tryGetFromCache(val -> STEAM_CACHE.getProfileInfo(Long.parseLong(val)), steamIds);

        // If all steamIds already founded from cache
        if (steamProfileInfoCached.getNeedToFound().size() <= 0)
            return steamProfileInfoCached.getFoundedList();

        final String value = steamProfileInfoCached.getNeedToFound().stream().collect(Collectors.joining(","));

        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("key", bot.getBotConfig().getApiKey());
        params.put("steamids", value);

        final String response = bot.getSteamWeb().fetch(REQ_PLAYER_INFO, new HttpParameters(params, HttpMethod.GET));

        final ApiResponse<GetPlayerSummaries> getPlayerSummaries =
                Json.getInstance().fromJson(response, new TypeReference<ApiResponse<GetPlayerSummaries>>() {
                });

        final List<SteamProfileInfo> plyProfiles = getPlayerSummaries.getResponse().getPlayers();
        plyProfiles.addAll(steamProfileInfoCached.getFoundedList());

        // Collect all profiles into map (FOR CACHE). Key - steamId, Value - steamProfile
        final Map<Long, SteamProfileInfo> profilesMap = plyProfiles.stream()
                .collect(Collectors.toMap(obj -> Long.parseLong(obj.getSteamId()),
                        obj -> obj,
                        (obj, obj2) -> {
                            // Duplicate key, ignore obj2
                            return obj;
                        }));
        STEAM_CACHE.saveProfile(profilesMap);

        return plyProfiles;
    }

    public Optional<SteamProfileInfo> getProfileInfo(String steamId) {
        final SteamProfileInfo cachedProfile = STEAM_CACHE.getProfileInfo(Long.parseLong(steamId));

        if (cachedProfile != null) return Optional.of(cachedProfile);

        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("key", bot.getBotConfig().getApiKey());
        params.put("steamids", steamId);

        final String response = bot.getSteamWeb().fetch(REQ_PLAYER_INFO, new HttpParameters(params, HttpMethod.GET));

        final ApiResponse<GetPlayerSummaries> getPlayerSummaries
                = Json.getInstance().fromJson(response, new TypeReference<ApiResponse<GetPlayerSummaries>>() {
        });

        final List<SteamProfileInfo> players = getPlayerSummaries.getResponse().getPlayers();
        if (!players.isEmpty()) {
            final SteamProfileInfo profileInfo = players.get(0);
            STEAM_CACHE.saveProfile(Long.parseLong(steamId), profileInfo);
            return Optional.ofNullable(profileInfo);
        }

        return Optional.empty();
    }

    public Optional<ItemAssetInfo> getItemInfo(GameType gameType, String classId) {
        return getItemInfo(String.valueOf(gameType.getAppid()), classId);
    }

    public List<ItemAssetInfo> getItemInfo(GameType gameType, List<String> classIdList) {
        return getItemInfo(bot.getBotConfig().getApiKey(), String.valueOf(gameType.getAppid()), classIdList);
    }

    public Optional<ItemAssetInfo> getItemInfo(String appId, String classId) {
        final int appID = Integer.parseInt(appId);
        final ItemAssetInfo itemInfo = STEAM_CACHE.getItemInfo(Long.parseLong(classId), Integer.parseInt(appId));

        if (itemInfo != null) return Optional.of(itemInfo);

        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
        params.put("key", bot.getBotConfig().getApiKey());
        params.put("appid", appId);
        params.put("classid0", classId);
        params.put("class_count", "1");

        final String response = bot.getSteamWeb().fetch(REQ_ASSET_INFO, new HttpParameters(params, HttpMethod.GET));
        final GetAssetClassInfo assetClassInfo = Json.getInstance().fromJson(response, GetAssetClassInfo.class);

        if (!assetClassInfo.getItemAssetInfoList().isEmpty()) {
            final ItemAssetInfo itemAssetInfo = assetClassInfo.getItemAssetInfoList().get(0);
            STEAM_CACHE.saveItemInfo(itemAssetInfo, appID);
            return Optional.of(itemAssetInfo);
        }

        return Optional.empty();
    }

    public List<ItemAssetInfo> getItemInfo(String apiKey, String appId, List<String> classIdList) {
        final int appID = Integer.parseInt(appId);
        final CacheDataHolder<ItemAssetInfo> itemAssetInfoCached =
                tryGetFromCache(val -> STEAM_CACHE.getItemInfo(Long.parseLong(val), appID));

        if (itemAssetInfoCached.getNeedToFound().size() <= 0)
            return itemAssetInfoCached.getFoundedList();

        final List<String> needToFound = itemAssetInfoCached.getNeedToFound();
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(needToFound.size() + 2);
        params.put("key", apiKey);
        params.put("appid", appId);
        for (int i = 0; i < needToFound.size(); i++) {
            params.put("classid" + i, classIdList.get(i));
        }
        params.put("class_count", String.valueOf(needToFound.size()));

        final String response = bot.getSteamWeb().fetch(REQ_ASSET_INFO, new HttpParameters(params, HttpMethod.GET));
        final GetAssetClassInfo assetClassInfo = Json.getInstance().fromJson(response, GetAssetClassInfo.class);
        assetClassInfo.getItemAssetInfoList().addAll(itemAssetInfoCached.getFoundedList());

        STEAM_CACHE.saveItemInfo(assetClassInfo.getItemAssetInfoList(), appID);

        return assetClassInfo.getItemAssetInfoList();
    }

    public List<Match> getDotaMatchHistory(int matchRequestCount) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("matches_requested", String.valueOf(matchRequestCount));
        params.put("key", bot.getBotConfig().getApiKey());
        final String response = bot.getSteamWeb().fetch(REQ_DOTA2_MATCH_HISTORY, new HttpParameters(params, HttpMethod.GET));
        final ResultWrapper<GetMatchHistory> resultWrapper = Json.getInstance().fromJson(response, new TypeReference<ResultWrapper<GetMatchHistory>>() {
        });

        return resultWrapper.getResult().getMatches();
    }

    private static final class CacheDataHolder<T> {

        private final List<T> foundedList;

        private final List<String> needToFound;

        CacheDataHolder(List<T> foundedList, List<String> needToFound) {
            this.foundedList = foundedList;
            this.needToFound = needToFound;
        }

        List<T> getFoundedList() {
            return foundedList;
        }

        List<String> getNeedToFound() {
            return needToFound;
        }
    }

    private static final class ResultWrapper<T> {

        private T result;

        public T getResult() {
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }
}
