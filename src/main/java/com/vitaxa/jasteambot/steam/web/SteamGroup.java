package com.vitaxa.jasteambot.steam.web;

import com.vitaxa.jasteambot.steam.web.model.GroupAnnounceItem;
import com.vitaxa.jasteambot.steam.web.model.GroupEventServer;
import com.vitaxa.jasteambot.steam.web.model.GroupEventType;
import uk.co.thomasc.steamkit.types.SteamID;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SteamGroup {

    private final Long id;

    private final SteamID gid;

    private final String name;

    private final String url;

    private final String headline;

    private final String summary;

    private final String avatarIcon;

    private final String avatarMedium;

    private final String avatarFull;

    private final Long memberCount;

    private final Long membersInChat;

    private final Long membersInGame;

    private final Long membersOnline;

    private final SteamCommunity steamCommunity;

    SteamGroup(Builder builder) {
        this.id = Objects.requireNonNull(builder.getId(), "id can't be null");
        this.gid = new SteamID(id);
        this.name = builder.getName();
        this.url = builder.getUrl();
        this.headline = builder.getHeadline();
        this.summary = builder.getSummary();
        this.avatarIcon = builder.getAvatarIcon();
        this.avatarMedium = builder.getAvatarMedium();
        this.avatarFull = builder.getAvatarFull();
        this.memberCount = builder.getMemberCount();
        this.membersInChat = builder.getMembersInChat();
        this.membersInGame = builder.getMembersInGame();
        this.membersOnline = builder.getMembersOnline();
        this.steamCommunity = builder.getSteamCommunity();
    }

    static Builder newBuilder(SteamCommunity steamCommunity) {
        return new Builder(steamCommunity);
    }

    public List<SteamID> getMembers() {
        return steamCommunity.getGroupMembers(gid);
    }

    public void join() {
        steamCommunity.joinGroup(gid);
    }

    public void leave() {
        steamCommunity.leaveGroup(gid);
    }

    public List<GroupAnnounceItem> getAllAnnouncements(LocalDateTime time) {
        return steamCommunity.getAllGroupAnnouncements(gid, time);
    }

    public void postAnnouncement(String title, String content) {
        steamCommunity.postGroupAnnouncement(gid, title, content);
    }

    public void editAnnouncement(String announcementID, String title, String content) {
        steamCommunity.editGroupAnnouncement(gid, announcementID, title, content);
    }

    public void deleteAnnouncement(String announcementID) {
        steamCommunity.deleteGroupAnnouncement(gid, announcementID);
    }

    public void scheduleEvent(String name, GroupEventType type, String description, LocalDateTime time, GroupEventServer server) {
        steamCommunity.scheduleGroupEvent(gid, name, type, description, time, server);
    }

    public void editEvent(String eventId, String name, GroupEventType type, String description, LocalDateTime time, GroupEventServer server) {
        steamCommunity.editGroupEvent(gid, eventId, name, type, description, time, server);
    }

    public void deleteEvent(String eventId) {
        steamCommunity.deleteGroupEvent(gid, eventId);
    }

    public void setPlayerOfTheWeek(SteamID userId) {
        steamCommunity.setGroupPlayerOfTheWeek(gid, userId);
    }

    public void kick(SteamID userId) {
        steamCommunity.kickGroupMember(gid, userId);
    }

    /**
     * Get requests to join this restricted group.
     */
    public List<SteamID> getJoinRequest() {
        return steamCommunity.getGroupJoinRequests(gid);
    }

    /**
     * Respond to one or more join requests to this restricted group.
     *
     * @param steamIDs - The SteamIDs of the users you want to approve or deny membership for (or a single value)
     * @param approve  - True to put them in the group, false to deny their membership
     */
    public void respondToJoinRequests(Collection<SteamID> steamIDs, boolean approve) {
        steamCommunity.respondToGroupJoinRequests(gid, steamIDs, approve);
    }

    /**
     * Respond to *ALL* pending group-join requests for this group.
     *
     * @param approve - True to allow everyone who requested into the group, false to not
     */
    public void respondToAllJoinRequests(boolean approve) {
        steamCommunity.respondToAllGroupJoinRequests(gid, approve);
    }

    public static final class Builder {

        private final SteamCommunity steamCommunity;

        private Long id;

        private String name;

        private String url;

        private String headline;

        private String summary;

        private String avatarIcon;

        private String avatarMedium;

        private String avatarFull;

        private Long memberCount;

        private Long membersInChat;

        private Long membersInGame;

        private Long membersOnline;

        private Builder(SteamCommunity steamCommunity) {
            this.steamCommunity = steamCommunity;
        }

        public Long getId() {
            return id;
        }

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public String getName() {
            return name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getUrl() {
            return url;
        }

        public Builder setUrl(String url) {
            this.url = url;
            return this;
        }

        public String getHeadline() {
            return headline;
        }

        public Builder setHeadline(String headline) {
            this.headline = headline;
            return this;
        }

        public String getSummary() {
            return summary;
        }

        public Builder setSummary(String summary) {
            this.summary = summary;
            return this;
        }

        public String getAvatarIcon() {
            return avatarIcon;
        }

        public Builder setAvatarIcon(String avatarIcon) {
            this.avatarIcon = avatarIcon;
            return this;
        }

        public String getAvatarMedium() {
            return avatarMedium;
        }

        public Builder setAvatarMedium(String avatarMedium) {
            this.avatarMedium = avatarMedium;
            return this;
        }

        public String getAvatarFull() {
            return avatarFull;
        }

        public Builder setAvatarFull(String avatarFull) {
            this.avatarFull = avatarFull;
            return this;
        }

        public Long getMemberCount() {
            return memberCount;
        }

        public Builder setMemberCount(Long memberCount) {
            this.memberCount = memberCount;
            return this;
        }

        public Long getMembersInChat() {
            return membersInChat;
        }

        public Builder setMembersInChat(Long membersInChat) {
            this.membersInChat = membersInChat;
            return this;
        }

        public Long getMembersInGame() {
            return membersInGame;
        }

        public Builder setMembersInGame(Long membersInGame) {
            this.membersInGame = membersInGame;
            return this;
        }

        public Long getMembersOnline() {
            return membersOnline;
        }

        public Builder setMembersOnline(Long membersOnline) {
            this.membersOnline = membersOnline;
            return this;
        }

        public SteamCommunity getSteamCommunity() {
            return steamCommunity;
        }

        public SteamGroup build() {
            return new SteamGroup(this);
        }
    }
}
