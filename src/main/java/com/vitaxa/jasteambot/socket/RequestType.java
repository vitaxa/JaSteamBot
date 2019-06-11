package com.vitaxa.jasteambot.socket;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.stream.EnumSerializer;

import java.io.IOException;

public enum RequestType implements EnumSerializer.Itf {

    BOT_ALL_INFO(1); // Get all bots info

    private static final EnumSerializer<RequestType> SERIALIZER = new EnumSerializer<>(RequestType.class);
    private final int n;

    RequestType(int n) {
        this.n = n;
    }

    public static RequestType read(HInput input) throws IOException {
        return SERIALIZER.read(input);
    }

    @Override
    public int getNumber() {
        return n;
    }

}
