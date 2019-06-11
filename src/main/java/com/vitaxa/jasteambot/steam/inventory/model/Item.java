package com.vitaxa.jasteambot.steam.inventory.model;

import java.util.List;

public class Item {

    private final long id;

    private final long classId;

    private final long instanceId;

    private final int amount;

    private final ItemDescription description;

    private final List<ItemTag> tagList;

    public Item(Builder builder) {
        this.id = builder.getId();
        this.classId = builder.getClassId();
        this.instanceId = builder.getInstanceId();
        this.amount = builder.getAmount();
        this.description = builder.getDescription();
        this.tagList = builder.getTagList();
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public long getId() {
        return id;
    }

    public long getClassId() {
        return classId;
    }

    public long getInstanceId() {
        return instanceId;
    }

    public int getAmount() {
        return amount;
    }

    public ItemDescription getDescription() {
        return description;
    }

    public List<ItemTag> getTagList() {
        return tagList;
    }

    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", classId=" + classId +
                ", instanceId=" + instanceId +
                ", amount=" + amount +
                ", description=" + description +
                ", tagList=" + tagList +
                '}';
    }

    public final static class Builder {

        private long id;

        private long classId;

        private long instanceId;

        private int amount;

        private ItemDescription description;

        private List<ItemTag> tagList;

        private Builder() {
        }

        public long getId() {
            return id;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public long getClassId() {
            return classId;
        }

        public Builder setClassId(long classId) {
            this.classId = classId;
            return this;
        }

        public long getInstanceId() {
            return instanceId;
        }

        public Builder setInstanceId(long instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public int getAmount() {
            return amount;
        }

        public Builder setAmount(int amount) {
            this.amount = amount;
            return this;
        }

        public ItemDescription getDescription() {
            return description;
        }

        public Builder setDescription(ItemDescription description) {
            this.description = description;
            return this;
        }

        public List<ItemTag> getTagList() {
            return tagList;
        }

        public Builder setTagList(List<ItemTag> tagList) {
            this.tagList = tagList;
            return this;
        }

        public Item build() {
            return new Item(this);
        }
    }

}
