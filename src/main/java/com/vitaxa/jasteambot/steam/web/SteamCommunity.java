package com.vitaxa.jasteambot.steam.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.vitaxa.jasteambot.Bot;
import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.steam.GameType;
import com.vitaxa.jasteambot.steam.cache.SteamCache;
import com.vitaxa.jasteambot.steam.web.http.HttpMethod;
import com.vitaxa.jasteambot.steam.web.http.HttpParameters;
import com.vitaxa.jasteambot.steam.web.model.*;
import com.vitaxa.steamauth.helper.Json;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import uk.co.thomasc.steamkit.base.generated.enums.EAccountType;
import uk.co.thomasc.steamkit.base.generated.enums.EUniverse;
import uk.co.thomasc.steamkit.types.SteamID;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SteamCommunity {

    private static final String PRICE_OVERVIEW_URL = "https://steamcommunity.com/market/priceoverview/";

    private static final String REQ_NOTIFICATION_COUNTS = "https://steamcommunity.com/actions/GetNotificationCounts";

    private static final String REQ_MY_INVENTORY = "https://steamcommunity.com/my/inventory";

    private static final String REQ_WEB_API_KEY = "https://steamcommunity.com/dev/apikey?l=english";

    private static final String REQ_REG_WEB_API_KEY = "https://steamcommunity.com/dev/registerkey?l=english";

    private static final SteamCache STEAM_CACHE = SteamCache.getInstance();

    private final Bot bot;

    public SteamCommunity(Bot bot) {
        this.bot = bot;
    }

    /**
     * This method is similar to {@link #getMarketPrice(int currency, String itemHashName, long appId)}
     */
    public ItemMarketPrice getMarketPrice(int currency, String itemHashName, GameType gameType) {
        return getMarketPrice(currency, itemHashName, gameType.getAppid());
    }

    /**
     * This method is similar to {@link #getMarketPrice(int currency, String itemHashName, long appId)}
     */
    public List<ItemMarketPrice> getMarketPrice(int currency, String[] itemHashNames, long appId) {
        return getMarketPrice(currency, Arrays.asList(itemHashNames), appId);
    }

    /**
     * This method is similar to {@link #getMarketPrice(int currency, String itemHashName, long appId)}
     */
    public List<ItemMarketPrice> getMarketPrice(int currency, Collection<String> itemHashNames, long appId) {
        List<ItemMarketPrice> itemMarketPriceList = new ArrayList<>(itemHashNames.size());
        for (String itemHashName : itemHashNames) {
            final ItemMarketPrice marketPrice = getMarketPrice(currency, itemHashName, appId);
            if (marketPrice != null) {
                itemMarketPriceList.add(marketPrice);
            }
        }
        return itemMarketPriceList;
    }

    /**
     * Return item market price
     *
     * @param currency     currency number. Example: 1 - $, 2 - £, 3 - €
     * @param itemHashName item marketHashName from description
     * @param appId        game appId
     * @return lowest and median price for item. null if bad steam response
     */
    public ItemMarketPrice getMarketPrice(int currency, String itemHashName, long appId) {
        ItemMarketPrice itemMarketPrice = STEAM_CACHE.getItemMarketPrice(currency, itemHashName, appId);

        if (itemMarketPrice != null) return itemMarketPrice;

        Map<String, String> params = MapHelper.newHashMapWithExpectedSize(3);
        params.put("currency", String.valueOf(currency));
        params.put("appid", String.valueOf(appId));
        params.put("market_hash_name", itemHashName);
        final String response = bot.getSteamWeb().fetch(PRICE_OVERVIEW_URL, new HttpParameters(params, HttpMethod.GET));
        final JsonNode jsonNode = Json.getInstance().nodeFromJson(response);
        final JsonNode successElement = jsonNode.get("success");
        if (successElement != null && successElement.asBoolean(false)) {
            final JsonNode lowestPrice = jsonNode.get("lowest_price");
            final JsonNode medianPrice = jsonNode.get("median_price");
            itemMarketPrice = new ItemMarketPrice(currency, itemHashName, medianPrice.asText(),
                    lowestPrice.asText(), GameType.byNum(appId));
            STEAM_CACHE.saveItemMarketPrice(itemMarketPrice);
            return itemMarketPrice;
        }
        return null;
    }

    public Notifications getNotifications() {
        final String response = bot.getSteamWeb().fetch(REQ_NOTIFICATION_COUNTS, new HttpParameters(HttpMethod.GET));
        return Json.getInstance().fromJson(response, Notifications.class);
    }

    public void resetItemNotifications() {
        bot.getSteamWeb().fetch(REQ_MY_INVENTORY, new HttpParameters(HttpMethod.GET));
    }

    public String getWebApiKey(String domain) {
        final RequestConfig reqConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
        final String response = bot.getSteamWeb().fetch(REQ_WEB_API_KEY, new HttpParameters(HttpMethod.GET), reqConfig);
        final Pattern accessDeniedPattern = Pattern.compile("<h2>Access Denied<\\/h2>");
        if (accessDeniedPattern.matcher(response).find()) {
            throw new IllegalStateException("Access Denied");
        }
        final Pattern needMailValidPattern = Pattern.compile("You must have a validated email address to create a Steam Web API key.");
        if (needMailValidPattern.matcher(response).find()) {
            throw new IllegalStateException("You must have a validated email address to create a Steam Web API key.");
        }
        final Matcher matcher = Pattern.compile("<p>Key: ([0-9A-F]+)<\\/p>").matcher(response);
        if (matcher.find()) {
            // We already have an API key registered
            return matcher.group(1);
        } else {
            // We need to register a new API key
            final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
            params.put("domain", domain);
            params.put("agreeToTerms", "agreed");
            params.put("sessionid", bot.getSteamWeb().getSessionId());
            params.put("Submit", "Register");
            bot.getSteamWeb().fetch(REQ_REG_WEB_API_KEY, new HttpParameters(params, HttpMethod.POST));

            return getWebApiKey(domain);
        }
    }

    public TradeUrl getTradeURL() {
        final String response = profileRequest("tradeoffers/privacy", HttpMethod.GET);
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("Something went wrong. Empty profile response");
        }
        final Pattern pattern = Pattern.compile("https?:\\/\\/(www.)?steamcommunity.com\\/tradeoffer\\/new\\/?\\?partner=\\d+(&|&amp;)token=([a-zA-Z0-9-_]+)");
        final Matcher matcher = pattern.matcher(response);
        if (!matcher.find()) {
            throw new IllegalStateException("Can't find trade url. Wrong response");
        }

        return new TradeUrl(matcher.group(0), matcher.group(3));
    }

    public TradeUrl changeTradeURL() {
        final String response = profileRequest("tradeoffers/newtradeurl", HttpMethod.POST);
        if (response == null || response.isEmpty()) {
            throw new IllegalStateException("Something went wrong. Empty profile response");
        }
        final String newToken = response.replace("\"", ""); // Remove quotes from token
        final String url = "https://steamcommunity.com/tradeoffer/new/?partner=" + bot.getSteamClient().getSteamID().getAccountID()
                + "&token=" + newToken;

        return new TradeUrl(url, newToken);
    }

    public void joinGroup(SteamID gid) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("action", "join");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64(),
                new HttpParameters(params, HttpMethod.POST));
    }

    public void leaveGroup(SteamID gid) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("action", "join");
        params.put("groupId", String.valueOf(gid.convertToUInt64()));
        profileRequest("home_process", new HttpParameters(params, HttpMethod.POST));
    }

    public List<GroupAnnounceItem> getAllGroupAnnouncements(SteamID gid, LocalDateTime time) {
        final String response = bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/rss/",
                new HttpParameters(HttpMethod.GET));
        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(response));
            final List<GroupAnnounceItem> groupAnnounceItemList = new ArrayList<>();
            GroupAnnounceItem.Builder groupAnnouncementItemBuilder = null;
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("item")) {
                        groupAnnouncementItemBuilder = GroupAnnounceItem.newBuilder();
                    }

                    if (groupAnnouncementItemBuilder == null) continue;

                    if (reader.getLocalName().equalsIgnoreCase("title")) {
                        groupAnnouncementItemBuilder.setTitle(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("description")) {
                        groupAnnouncementItemBuilder.setDescription(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("link")) {
                        final String link = reader.getElementText();
                        groupAnnouncementItemBuilder.setLink(link);
                        final String[] splitLink = link.split("/");
                        groupAnnouncementItemBuilder.setAid(splitLink[splitLink.length - 1]);

                    }
                    if (reader.getLocalName().equalsIgnoreCase("pubDate")) {
                        final ZonedDateTime zonedDateTime = ZonedDateTime.parse(reader.getElementText(),
                                DateTimeFormatter.RFC_1123_DATE_TIME);
                        groupAnnouncementItemBuilder.setPubDate(zonedDateTime);
                    }
                    if (reader.getLocalName().equalsIgnoreCase("author")) {
                        groupAnnouncementItemBuilder.setAuthor(reader.getElementText());
                    }
                }
                if (reader.getEventType() == XMLStreamConstants.END_ELEMENT) {
                    if (groupAnnouncementItemBuilder == null) continue;
                    if (reader.getLocalName().equalsIgnoreCase("item")) {
                        groupAnnounceItemList.add(groupAnnouncementItemBuilder.build());
                    }
                }
            }
            return groupAnnounceItemList.stream()
                    .filter(groupAnnounceItem -> groupAnnounceItem.getPubDate().toLocalDateTime().isAfter(time))
                    .collect(Collectors.toList());
        } catch (XMLStreamException e) {
            bot.getLog().error("Can't parse xml (get all group announcements)", e);
        }
        return null;
    }

    public void postGroupAnnouncement(SteamID gid, String title, String content) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(5);
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("action", "post");
        params.put("headline", title);
        params.put("body", content);
        params.put("languages[0][headline]", title);
        params.put("languages[0][body]", content);
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/announcement",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void editGroupAnnouncement(SteamID gid, String aid, String title, String content) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(7);
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("gid", aid);
        params.put("action", "update");
        params.put("headline", title);
        params.put("body", content);
        params.put("languages[0][headline]", title);
        params.put("languages[0][body]", content);
        params.put("languages[0][updated]", "1");
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/announcements",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void deleteGroupAnnouncement(SteamID gid, String aid) {
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/announcements/delete/"
                + aid + "?sessionID=" + bot.getSteamWeb().getSessionId(), new HttpParameters(HttpMethod.GET));
    }

    public void scheduleGroupEvent(SteamID gid, String name, long appID, String desc, GroupEventServer server) {
        scheduleGroupEvent(gid, name, null, appID, desc, null, server);
    }

    public void scheduleGroupEvent(SteamID gid, String name, GroupEventType type, String desc, GroupEventServer server) {
        scheduleGroupEvent(gid, name, type, null, desc, null, server);
    }

    public void scheduleGroupEvent(SteamID gid, String name, long appID, String desc,
                                   LocalDateTime time, GroupEventServer server) {
        scheduleGroupEvent(gid, name, null, appID, desc, time, server);
    }

    public void scheduleGroupEvent(SteamID gid, String name, GroupEventType type, String desc,
                                   LocalDateTime time, GroupEventServer server) {
        scheduleGroupEvent(gid, name, type, null, desc, time, server);
    }

    public void scheduleGroupEvent(SteamID gid, String name, GroupEventType type, Long appID, String desc,
                                   LocalDateTime time, GroupEventServer server) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(15);
        params.put("action", "newEvent");
        params.put("name", name);
        _groupEventAddParams(params, type, appID, desc, time, server);
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/eventEdit",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void editGroupEvent(SteamID gid, String eventId, String name, long appID, String desc, GroupEventServer server) {
        editGroupEvent(gid, eventId, name, null, appID, desc, null, server);
    }

    public void editGroupEvent(SteamID gid, String eventId, String name, GroupEventType type, String desc, GroupEventServer server) {
        editGroupEvent(gid, eventId, name, type, null, desc, null, server);
    }

    public void editGroupEvent(SteamID gid, String eventId, String name, long appID, String desc,
                               LocalDateTime time, GroupEventServer server) {
        editGroupEvent(gid, eventId, name, null, appID, desc, time, server);
    }

    public void editGroupEvent(SteamID gid, String eventId, String name, GroupEventType type, String desc,
                               LocalDateTime time, GroupEventServer server) {
        editGroupEvent(gid, eventId, name, type, null, desc, time, server);
    }

    public void editGroupEvent(SteamID gid, String eventId, String name, GroupEventType type, Long appID, String desc,
                               LocalDateTime time, GroupEventServer server) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(15);
        params.put("action", "updateEvent");
        params.put("eventID", eventId);
        _groupEventAddParams(params, type, appID, desc, time, server);
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/eventEdit",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void deleteGroupEvent(SteamID gid, String eventId) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(3);
        params.put("action", "deleteEvent");
        params.put("eventID", eventId);
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/eventEdit",
                new HttpParameters(params, HttpMethod.POST));
    }

    private void _groupEventAddParams(Map<String, String> params, GroupEventType type, Long appID, String desc,
                                      LocalDateTime time, GroupEventServer server) {
        params.put("sessionid", bot.getSteamWeb().getSessionId());
        params.put("tzOffset", String.valueOf((ZonedDateTime.now().getOffset().getTotalSeconds() * -60)));
        if (appID != null) {
            params.put("type", "GameEvent");
            params.put("appID", String.valueOf(appID));
        } else {
            params.put("type", type.getValue());
            params.put("appID", "");
        }
        params.put("serverIP", server.getIp());
        params.put("serverPassword", server.getPassword());
        params.put("notes", desc);
        params.put("eventQuickTime", "now");
        if (time == null) {
            params.put("startDate", "MM/DD/YY");
            params.put("startHour", "12");
            params.put("startMinute", "00");
            params.put("startAMPM", "PM");
            params.put("timeChoice", "quick");
        } else {
            params.put("startDate", (time.getMonthValue() + 1 < 10 ? "0" : "") + (time.getMonthValue() + 1) + "/"
                    + (time.getDayOfMonth() < 10 ? "0" : "") + time.toLocalDate() + "/" + time.getYear());
            params.put("startHour", time.getHour() == 0 ? "12" : String.valueOf(time.getHour() > 12 ? time.getHour() - 12 : time.getHour()));
            params.put("startMinute", (time.getMinute() < 10 ? "0" : "") + time.getMinute());
            params.put("startAMPM", time.getHour() <= 12 ? "AM" : "PM");
            params.put("timeChoice", "specific");
        }
    }

    public void setGroupPlayerOfTheWeek(SteamID gid, SteamID userId) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
        params.put("xml", "1");
        params.put("action", "potw");
        params.put("memberId", userId.render(true));
        params.put("sessionid", bot.getSteamWeb().getSessionId());
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/potwEdit",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void kickGroupMember(SteamID gid, SteamID userId) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("action", "kick");
        params.put("memberId", String.valueOf(userId.convertToUInt64()));
        params.put("queryString", "");
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/membersManage",
                new HttpParameters(params, HttpMethod.POST));
    }

    /**
     * Get requests to join a restricted group.
     *
     * @param gid The SteamID of the group you want to manage
     */
    public List<SteamID> getGroupJoinRequests(SteamID gid) {
        final String url = "https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/joinRequestsManage";
        final String response = bot.getSteamWeb().fetch(url, new HttpParameters(HttpMethod.GET));
        final Pattern compile = Pattern.compile("/JoinRequests_ApproveDenyUser\\(\\W*['\"](\\d+)['\"],\\W0\\W\\)/");
        final Matcher matcher = compile.matcher(response);
        final List<SteamID> requests = new ArrayList<>();
        int count = 0;
        while (matcher.find()) {
            count++;
            final String group = matcher.group(count);
            final SteamID steamID = new SteamID();
            steamID.setFromSteam3String("[U:1:" + compile.matcher(matcher.group(count)).group(1) + "]");
            requests.add(steamID);
        }
        return requests;
    }

    /**
     * Respond to one or more join requests to a restricted group.
     *
     * @param gid      The SteamID of the group you want to manage
     * @param steamIDs The SteamIDs of the users you want to approve or deny membership for (or a single value)
     * @param approve  True to put them in the group, false to deny their membership
     */
    public void respondToGroupJoinRequests(SteamID gid, Collection<SteamID> steamIDs, boolean approve) {
        final String rgAccounts = steamIDs.stream()
                .map(steamID -> String.valueOf(steamID.convertToUInt64()))
                .collect(Collectors.joining(","));
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
        params.put("rgAccounts", rgAccounts);
        params.put("bapprove", approve ? "1" : "0");
        params.put("json", "1");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/joinRequestsManage",
                new HttpParameters(params, HttpMethod.POST));
    }

    /**
     * Respond to *ALL* pending group-join requests for a particular group.
     *
     * @param gid     The SteamID of the group you want to manage
     * @param approve True to allow everyone who requested into the group, false to not
     */
    public void respondToAllGroupJoinRequests(SteamID gid, boolean approve) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(4);
        params.put("bapprove", approve ? "1" : "0");
        params.put("json", "1");
        params.put("action", "bulkrespond");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        bot.getSteamWeb().fetch("https://steamcommunity.com/gid/" + gid.convertToUInt64() + "/joinRequestsManage",
                new HttpParameters(params, HttpMethod.POST));
    }

    public SteamGroup getSteamGroup(SteamID steamID) {
        if (steamID.getAccountUniverse() != EUniverse.Public || steamID.getAccountType() != EAccountType.Clan) {
            throw new IllegalArgumentException("SteamID must stand for a clan account in the public universe");
        }
        final String url = "https://steamcommunity.com/gid/" + steamID.convertToUInt64() + "/memberslistxml/?xml=1";
        return _getSteamGroup(url);
    }

    public SteamGroup getSteamGroup(String gid) {
        final String url = "https://steamcommunity.com/groups/" + gid + "/memberslistxml/?xml=1";
        return _getSteamGroup(url);
    }

    private SteamGroup _getSteamGroup(String url) {
        final String response = bot.getSteamWeb().fetch(url, new HttpParameters(HttpMethod.GET));
        try {
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(response));
            final SteamGroup.Builder groupBuilder = SteamGroup.newBuilder(this);
            boolean ignoreFirstMemberCount = true;
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("groupID64")) {
                        groupBuilder.setId(Long.valueOf(reader.getElementText()));
                    }
                    if (reader.getLocalName().equalsIgnoreCase("groupName")) {
                        groupBuilder.setName(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("groupURL")) {
                        groupBuilder.setUrl(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("headline")) {
                        groupBuilder.setHeadline(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("summary")) {
                        groupBuilder.setSummary(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("avatarIcon")) {
                        groupBuilder.setAvatarIcon(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("avatarMedium")) {
                        groupBuilder.setAvatarMedium(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("avatarFull")) {
                        groupBuilder.setAvatarFull(reader.getElementText());
                    }
                    if (reader.getLocalName().equalsIgnoreCase("membersInChat")) {
                        groupBuilder.setMembersInChat(Long.valueOf(reader.getElementText()));
                    }
                    if (reader.getLocalName().equalsIgnoreCase("membersInGame")) {
                        groupBuilder.setMembersInGame(Long.valueOf(reader.getElementText()));
                    }
                    if (reader.getLocalName().equalsIgnoreCase("membersOnline")) {
                        groupBuilder.setMembersOnline(Long.valueOf(reader.getElementText()));
                    }
                    if (reader.getLocalName().equalsIgnoreCase("memberCount")) {
                        if (ignoreFirstMemberCount) {
                            ignoreFirstMemberCount = false;
                            continue;
                        }
                        groupBuilder.setMemberCount(Long.valueOf(reader.getElementText()));
                    }
                }
            }
            return groupBuilder.build();
        } catch (XMLStreamException e) {
            bot.getLog().error("Can't parse xml (get steam group)", e);
        }
        return null;
    }

    public <T> List<SteamID> getGroupMembers(T id) {
        final String url;
        if (id instanceof SteamID) {
            url = "http://steamcommunity.com/gid/" + ((SteamID) id).convertToUInt64() + "/memberslistxml/?xml=1";
        } else if (id instanceof String) {
            final SteamID steamID = new SteamID(((String) id));
            if (steamID.getAccountType() == EAccountType.Clan && steamID.isValid()) {
                url = "http://steamcommunity.com/gid/" + steamID.convertToUInt64() + "/memberslistxml/?xml=1";
            } else {
                url = "http://steamcommunity.com/groups/" + id + "/memberslistxml/?xml=1";
            }
        } else {
            throw new IllegalArgumentException("id unknown type");
        }

        final List<SteamID> members = new ArrayList<>();
        String nextPageLink = _getGroupMembers(url, members);
        while (nextPageLink != null && !nextPageLink.isEmpty()) {
            nextPageLink = _getGroupMembers(nextPageLink, members);
        }

        return members;
    }

    private String _getGroupMembers(String url, List<SteamID> members) {
        if (members == null) {
            members = new ArrayList<>();
        }
        try {
            final String response = bot.getSteamWeb().fetch(url, new HttpParameters(HttpMethod.GET));
            final XMLInputFactory factory = XMLInputFactory.newInstance();
            final XMLStreamReader reader = factory.createXMLStreamReader(new StringReader(response));
            String nextPageLink = null;
            while (reader.hasNext()) {
                reader.next();
                if (reader.getEventType() == XMLStreamConstants.START_ELEMENT) {
                    if (reader.getLocalName().equalsIgnoreCase("nextPageLink")) {
                        nextPageLink = reader.getElementText();
                    }
                    if (reader.getLocalName().equalsIgnoreCase("steamID64")) {
                        members.add(new SteamID(Long.parseLong(reader.getElementText())));
                    }
                }
            }
            return nextPageLink;
        } catch (XMLStreamException e) {
            bot.getLog().error("Can't parse group members xml", e);
        }
        return null;
    }

    public void addFriend(SteamID userID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(3);
        params.put("accept_invite", "0");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("steamid", String.valueOf(userID.convertToUInt64()));
        bot.getSteamWeb().fetch("https://steamcommunity.com/actions/AddFriendAjax",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void acceptFriendRequest(SteamID userID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(3);
        params.put("accept_invite", "1");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("steamid", String.valueOf(userID.convertToUInt64()));
        bot.getSteamWeb().fetch("https://steamcommunity.com/actions/AddFriendAjax",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void removeFriend(SteamID userID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("steamid", String.valueOf(userID.convertToUInt64()));
        bot.getSteamWeb().fetch("https://steamcommunity.com/actions/RemoveFriendAjax",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void blockCommunication(SteamID userID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("steamid", String.valueOf(userID.convertToUInt64()));
        bot.getSteamWeb().fetch("https://steamcommunity.com/actions/BlockUserAjax",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void unBlockCommunication(SteamID userID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(2);
        params.put("action", "unignore");
        params.put("friends[" + userID.convertToUInt64() + "]", "1");
        profileRequest("friends/blocked/", new HttpParameters(params, HttpMethod.POST));
    }

    public void postUserComment(SteamID userID, String message) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(3);
        params.put("comment", message);
        params.put("count", "6");
        params.put("sessionid", bot.getSteamWeb().getSessionId());
        bot.getSteamWeb().fetch("https://steamcommunity.com/comment/Profile/post/" + userID.convertToUInt64() + "/-1",
                new HttpParameters(params, HttpMethod.POST));
    }

    public void inviteUserToGroup(SteamID userID, SteamID groupID) {
        final Map<String, String> params = MapHelper.newHashMapWithExpectedSize(5);
        params.put("group", String.valueOf(groupID.convertToUInt64()));
        params.put("invitee", String.valueOf(groupID.convertToUInt64()));
        params.put("json", "1");
        params.put("sessionID", bot.getSteamWeb().getSessionId());
        params.put("type", "groupInvite");
        bot.getSteamWeb().fetch("https://steamcommunity.com/actions/GroupInvite", new HttpParameters(params, HttpMethod.POST));
    }

    private String profileRequest(String endPoint, HttpMethod httpMethod) {
        return profileRequest(endPoint, new HttpParameters(httpMethod));
    }

    private String profileRequest(String endPoint, HttpParameters httpParameters) {
        if (endPoint == null || endPoint.isEmpty()) {
            throw new IllegalArgumentException("endPoint can't be null or empty");
        }
        final String cacheKey = "profileUrl" + bot.getSteamClient().getSteamID().convertToUInt64();
        final String profileUrlCached = ((String) STEAM_CACHE.getCommonInfo(cacheKey));
        if (profileUrlCached == null || profileUrlCached.isEmpty()) {
            final RequestConfig reqConfig = RequestConfig.custom().setRedirectsEnabled(false).build();
            try (final CloseableHttpResponse response = bot.getSteamWeb().request(REQ_MY_INVENTORY,
                    new HttpParameters(HttpMethod.GET), reqConfig)) {
                final int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 302) {
                    throw new ClientProtocolException("Unexpected response status: " + statusCode);
                }
                String profileUrl = response.getHeaders("location")[0].getElements()[0].getName();
                final Pattern pattern = Pattern.compile("steamcommunity\\.com(/(id|profiles)/[^/]+)/?");
                final Matcher matcher = pattern.matcher(profileUrl);
                if (!matcher.find()) {
                    throw new IllegalStateException("Can't find profile url");
                }
                profileUrl = matcher.group(1);

                STEAM_CACHE.saveCommonInfo(cacheKey, profileUrl);

                return profileRequest(endPoint, profileUrl, httpParameters);
            } catch (IOException e) {
                bot.getLog().error("Profile request exception", e);
            }
        } else {
            return profileRequest(endPoint, profileUrlCached, httpParameters);
        }
        return null;
    }

    private String profileRequest(String endPoint, String profileUrl, HttpParameters httpParameters) {
        if (profileUrl == null || profileUrl.isEmpty()) {
            throw new IllegalArgumentException("profileUrl can't be null");
        }
        final String url = "https://steamcommunity.com" + profileUrl + "/" + endPoint;

        if (httpParameters.getMethod() == HttpMethod.POST) {
            httpParameters.getParams().putIfAbsent("sessionid", bot.getSteamWeb().getSessionId());
            return bot.getSteamWeb().fetch(url, httpParameters);
        }

        int loopCount = 0;
        String response = null;
        String tmpUrl = url;
        boolean wasRedirected = false;
        do {
            loopCount++;
            try (final CloseableHttpResponse httpResponse = bot.getSteamWeb().request(tmpUrl, new HttpParameters(HttpMethod.GET))) {
                final int statusCode = httpResponse.getStatusLine().getStatusCode();
                if (statusCode == 302) {
                    wasRedirected = true;
                    tmpUrl = httpResponse.getHeaders("location")[0].getElements()[0].getName();
                } else {
                    if (statusCode != 200) break;
                    response = IOHelper.decode(IOHelper.read(httpResponse.getEntity().getContent()));
                }
            } catch (IOException e) {
                bot.getLog().error("Exception while request: ", e);
            }
            if (loopCount >= 10) {
                throw new IllegalStateException("Infinity loop");
            }
        } while (response == null || response.isEmpty()); // Send request until get response (steam check your profile)

        // If not redirected then take response
        if (!wasRedirected) {
            return response;
        }

        // Send first request again
        return bot.getSteamWeb().fetch(url, new HttpParameters(HttpMethod.GET));
    }

}
