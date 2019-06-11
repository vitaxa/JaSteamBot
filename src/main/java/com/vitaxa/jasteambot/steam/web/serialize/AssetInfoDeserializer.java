package com.vitaxa.jasteambot.steam.web.serialize;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.vitaxa.jasteambot.helper.CommonHelper;
import com.vitaxa.jasteambot.steam.inventory.model.ItemTag;
import com.vitaxa.jasteambot.steam.web.model.GetAssetClassInfo;
import com.vitaxa.jasteambot.steam.web.model.ItemAssetInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AssetInfoDeserializer extends JsonDeserializer<GetAssetClassInfo> {

    @Override
    public GetAssetClassInfo deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final JsonNode jsonNode = jsonParser.getCodec().readTree(jsonParser);

        final JsonNode resultNode = jsonNode.get("result");

        if (resultNode == null || !resultNode.isObject())
            throw new JsonParseException(jsonParser, "Result null or not json object");

        final boolean success = resultNode.get("success").asBoolean();

        if (!success) return null;

        final List<ItemAssetInfo> itemAssetInfoList = new ArrayList<>();
        resultNode.elements().forEachRemaining(node -> {
            if (!node.isObject()) return;
            final JsonNode tagsNode = node.get("tags");
            final List<ItemTag> tagList = new ArrayList<>();
            if (tagsNode != null) {
                for (int i = 0; tagsNode.get(String.valueOf(i)) != null; i++) {
                    final JsonNode tagObject = tagsNode.get(String.valueOf(i));
                    final JsonNode tagNameElement = tagObject.get("name");
                    final JsonNode tagCategoryElement = tagObject.get("category");
                    tagList.add(new ItemTag(tagNameElement.asText(), tagCategoryElement.asText()));
                }
            }

            final JsonNode marketTradableRestrictionNode = node.get("market_tradable_restriction");
            final JsonNode fraudWarningsNode = node.get("fraudwarnings");
            final ItemAssetInfo itemAssetInfo = ItemAssetInfo.newBuilder().setClassId(node.get("classid").asText())
                    .setName(node.get("name").asText())
                    .setMarketHashName(node.get("market_hash_name").asText())
                    .setTradable(CommonHelper.toBoolean(node.get("tradable").asText()))
                    .setMarketable(CommonHelper.toBoolean(node.get("marketable").asText()))
                    .setMarketTradableRestriction(marketTradableRestrictionNode != null ? marketTradableRestrictionNode.asInt() : null)
                    .setFraudWarnings(fraudWarningsNode != null ? fraudWarningsNode.asText() : null)
                    .setTagList(tagList)
                    .build();
            itemAssetInfoList.add(itemAssetInfo);
        });

        return new GetAssetClassInfo(itemAssetInfoList);
    }
}
