package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;

public final class IntegerConfigEntry extends ConfigEntry<Integer> {
    public IntegerConfigEntry(int value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public IntegerConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readVarInt(), ro, 0);
    }

    @Override
    public Type getType() {
        return Type.INTEGER;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarInt(getValue());
    }
}