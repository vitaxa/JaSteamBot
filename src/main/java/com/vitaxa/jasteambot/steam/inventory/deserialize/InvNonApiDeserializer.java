package com.vitaxa.jasteambot.steam.inventory.deserialize;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.steam.inventory.Inventory;
import com.vitaxa.jasteambot.steam.inventory.model.Item;
import com.vitaxa.jasteambot.steam.inventory.model.ItemDescription;
import com.vitaxa.jasteambot.steam.inventory.model.ItemTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class InvNonApiDeserializer extends JsonDeserializer<Inventory> {

    private final static Logger LOG = LoggerFactory.getLogger(InvNonApiDeserializer.class);

    @Override
    public Inventory deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        final JsonNode node = jsonParser.getCodec().readTree(jsonParser);

        boolean success = node.get("success").asBoolean();
        if (!success)
            return new Inventory(false, Collections.emptyList(), null);

        final JsonNode rgInventory = node.get("rgInventory");
        final JsonNode rgDescriptions = node.get("rgDescriptions");

        final List<Item> itemList = new ArrayList<>();

        Long appId = null;

        final Iterator<JsonNode> elements = rgInventory.elements();
        while (elements.hasNext()) {
            final JsonNode jsonNode = elements.next();
            final long id = jsonNode.get("id").asLong();
            final long classId = jsonNode.get("classid").asLong();
            final long instanceId = jsonNode.get("instanceid").asLong();
            final int amount = jsonNode.get("amount").asInt();

            final JsonNode descriptionNode = rgDescriptions.get(classId + "_" + instanceId);
            appId = descriptionNode.get("appid").asLong();
            final String name = descriptionNode.get("name").asText();
            final String marketName = descriptionNode.get("market_name").asText();
            final String marketHashName = descriptionNode.get("market_hash_name").asText();
            final int tradable = descriptionNode.get("tradable").asInt();
            final int marketable = descriptionNode.get("marketable").asInt();
            final JsonNode marketTradableRestrictionNode = descriptionNode.get("market_tradable_restriction");
            final Integer marketTradableRestriction = marketTradableRestrictionNode != null ? marketTradableRestrictionNode.asInt() : null;
            final JsonNode cacheExpirationNode = descriptionNode.get("cache_expiration");
            LocalDateTime cacheExpiration = null;
            if (cacheExpirationNode != null) {
                cacheExpiration = CommonHelper.convertStringToDateTime(cacheExpirationNode.asText());
            }
            final JsonNode fraudWarningsNode = descriptionNode.get("fraudwarnings");
            String fraudWarnings = "";
            if (fraudWarningsNode != null && fraudWarningsNode.isArray()) {
                final JsonNode objNode = fraudWarningsNode.get(0);
                if (objNode != null) {
                    fraudWarnings = objNode.asText();
                }
            }

            final List<ItemTag> tagList = new ArrayList<>();
            final JsonNode tags = descriptionNode.get("tags");
            if (tags != null && tags.isArray()) {
                for (JsonNode tagNode : tags) {
                    final JsonNode tagNameElement = tagNode.get("name");
                    final JsonNode tagCategoryElement = tagNode.get("category");
                    tagList.add(new ItemTag(tagNameElement.asText(), tagCategoryElement.asText()));
                }
            }

            final ItemDescription itemDescription = ItemDescription.newBuilder().setAppId(appId)
                    .setName(name)
                    .setMarketName(marketName)
                    .setMarketHashName(marketHashName)
                    .setTradable(CommonHelper.toBoolean(tradable))
                    .setMarketable(CommonHelper.toBoolean(marketable))
                    .setMarketTradableRestriction(marketTradableRestriction)
                    .setCacheExpiration(cacheExpiration)
                    .setFraudwarnings(fraudWarnings)
                    .build();

            final Item item = Item.newBuilder().setId(id)
                    .setAmount(amount)
                    .setClassId(classId)
                    .setInstanceId(instanceId)
                    .setDescription(itemDescription)
                    .setTagList(tagList)
                    .build();

            itemList.add(item);
        }

        return new Inventory(true, itemList, appId);
    }
}

