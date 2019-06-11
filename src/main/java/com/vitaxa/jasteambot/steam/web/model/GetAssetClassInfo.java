package com.vitaxa.jasteambot.steam.web.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vitaxa.jasteambot.steam.web.serialize.AssetInfoDeserializer;

import java.util.List;

@JsonDeserialize(using = AssetInfoDeserializer.class)
public class GetAssetClassInfo {

    private List<ItemAssetInfo> itemAssetInfoList;

    public GetAssetClassInfo(List<ItemAssetInfo> itemAssetInfoList) {
        this.itemAssetInfoList = itemAssetInfoList;
    }

    public List<ItemAssetInfo> getItemAssetInfoList() {
        return itemAssetInfoList;
    }
}
