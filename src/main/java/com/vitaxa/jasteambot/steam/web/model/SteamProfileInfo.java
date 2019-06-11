package com.vitaxa.jasteambot.steam.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SteamProfileInfo {

    @JsonProperty("steamid")
    private String steamId;

    @JsonProperty("communityvisibilitystate")
    private short communityVisibilityState;

    @JsonProperty("profilestate")
    private short profileState;

    @JsonProperty("personaname")
    private String personaName;

    @JsonProperty("lastlogoff")
    private long lastLogoff;

    @JsonProperty("commentpermission")
    private short commentPermission;

    @JsonProperty("profileurl")
    private String profileUrl;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("avatarmedium")
    private String avatarMedium;

    @JsonProperty("avatarfull")
    private String avatarFull;

    @JsonProperty("personastate")
    private short personaState;

    @JsonProperty("primaryclanid")
    private String primaryClanId;

    @JsonProperty("timecreated")
    private long timeCreated;

    @JsonProperty("personastateflags")
    private short personaStateFlags;

    @JsonProperty("loccountrycode")
    private String locCountryCode;

    @JsonProperty("locstatecode")
    private String locStateCode;

    public String getSteamId() {
        return steamId;
    }

    public void setSteamId(String steamId) {
        this.steamId = steamId;
    }

    public short getCommunityVisibilityState() {
        return communityVisibilityState;
    }

    public void setCommunityVisibilityState(short communityVisibilityState) {
        this.communityVisibilityState = communityVisibilityState;
    }

    public short getProfileState() {
        return profileState;
    }

    public void setProfileState(short profileState) {
        this.profileState = profileState;
    }

    public String getPersonaName() {
        return personaName;
    }

    public void setPersonaName(String personaName) {
        this.personaName = personaName;
    }

    public long getLastLogoff() {
        return lastLogoff;
    }

    public void setLastLogoff(long lastLogoff) {
        this.lastLogoff = lastLogoff;
    }

    public short getCommentPermission() {
        return commentPermission;
    }

    public void setCommentPermission(short commentPermission) {
        this.commentPermission = commentPermission;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatarMedium() {
        return avatarMedium;
    }

    public void setAvatarMedium(String avatarMedium) {
        this.avatarMedium = avatarMedium;
    }

    public String getAvatarFull() {
        return avatarFull;
    }

    public void setAvatarFull(String avatarFull) {
        this.avatarFull = avatarFull;
    }

    public short getPersonaState() {
        return personaState;
    }

    public void setPersonaState(short personaState) {
        this.personaState = personaState;
    }

    public String getPrimaryClanId() {
        return primaryClanId;
    }

    public void setPrimaryClanId(String primaryClanId) {
        this.primaryClanId = primaryClanId;
    }

    public long getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(long timeCreated) {
        this.timeCreated = timeCreated;
    }

    public short getPersonaStateFlags() {
        return personaStateFlags;
    }

    public void setPersonaStateFlags(short personaStateFlags) {
        this.personaStateFlags = personaStateFlags;
    }

    public String getLocCountryCode() {
        return locCountryCode;
    }

    public void setLocCountryCode(String locCountryCode) {
        this.locCountryCode = locCountryCode;
    }

    public String getLocStateCode() {
        return locStateCode;
    }

    public void setLocStateCode(String locStateCode) {
        this.locStateCode = locStateCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SteamProfileInfo that = (SteamProfileInfo) o;

        return steamId != null ? steamId.equals(that.steamId) : that.steamId == null;
    }

    @Override
    public int hashCode() {
        return steamId != null ? steamId.hashCode() : 0;
    }
}

