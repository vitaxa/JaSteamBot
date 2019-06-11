package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;

public class LongConfigEntry extends ConfigEntry<Long> {
    public LongConfigEntry(long value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public LongConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readVarInt(), ro, 0);
    }

    @Override
    public ConfigEntry.Type getType() {
        return ConfigEntry.Type.LONG;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeVarLong(getValue());
    }
}
