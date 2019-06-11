package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;

public final class StringConfigEntry extends ConfigEntry<String> {
    public StringConfigEntry(String value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public StringConfigEntry(HInput input, boolean ro) throws IOException {
        this(input.readString(0), ro, 0);
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public void write(HOutput output) throws IOException {
        output.writeString(getValue(), 0);
    }

    @Override
    protected void uncheckedSetValue(String value) {
        super.uncheckedSetValue(value);
    }
}
