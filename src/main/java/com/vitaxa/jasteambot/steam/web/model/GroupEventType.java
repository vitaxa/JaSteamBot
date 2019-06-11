package com.vitaxa.jasteambot.steam.web.model;

public enum GroupEventType {

    CHAT_EVENT("ChatEvent"),

    OTHER_EVENT("OtherEvent"),

    PARTY_EVENT("PartyEvent"),

    MEETING_EVENT("MeetingEvent"),

    SPECIAL_CAUSE_EVENT("SpecialCauseEvent"),

    MUSIC_AND_ARTS_EVENT("MusicAndArtsEvent"),

    SPORTS_EVENT("SportsEvent"),

    TRIP_EVENT("TripEvent");

    private final String value;

    GroupEventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
