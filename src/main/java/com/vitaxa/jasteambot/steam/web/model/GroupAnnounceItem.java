package com.vitaxa.jasteambot.steam.web.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public class GroupAnnounceItem {

    private final String title;

    private final String description;

    private final String link;

    private final ZonedDateTime pubDate;

    private final String author;

    private final String aid;

    GroupAnnounceItem(Builder builder) {
        this.title = Objects.requireNonNull(builder.getTitle(), "title can't be null");
        this.description = Objects.requireNonNull(builder.getDescription(), "description can't be null");
        this.link = Objects.requireNonNull(builder.getLink(), "link can't be null");
        this.pubDate = Objects.requireNonNull(builder.getPubDate(), "pubDate can't be null");
        this.author = builder.getAuthor();
        this.aid = Objects.requireNonNull(builder.getAid(), "aid can't be null");
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getLink() {
        return link;
    }

    public ZonedDateTime getPubDate() {
        return pubDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getAid() {
        return aid;
    }

    public static final class Builder {

        private String title;

        private String description;

        private String link;

        private ZonedDateTime pubDate;

        private String author;

        private String aid;

        private Builder() {
        }

        public String getTitle() {
            return title;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public String getDescription() {
            return description;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public String getLink() {
            return link;
        }

        public Builder setLink(String link) {
            this.link = link;
            return this;
        }

        public ZonedDateTime getPubDate() {
            return pubDate;
        }

        public Builder setPubDate(ZonedDateTime pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        public String getAuthor() {
            return author;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public String getAid() {
            return aid;
        }

        public Builder setAid(String aid) {
            this.aid = aid;
            return this;
        }

        public GroupAnnounceItem build() {
            return new GroupAnnounceItem(this);
        }
    }

}
