package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;


public final class BooleanConfigEntry extends ConfigEntry<Boolean> {
    public BooleanConfigEntry(boolean value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public BooleanConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readBoolean(), ro, 0);
    }

    @Override
    public Type getType() {
        return Type.BOOLEAN;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeBoolean(getValue());
    }
}