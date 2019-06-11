package com.vitaxa.jasteambot.serialize.config;

import com.vitaxa.jasteambot.serialize.HOutput;
import com.vitaxa.jasteambot.serialize.config.entry.BlockConfigEntry;
import com.vitaxa.jasteambot.serialize.stream.StreamObject;

import java.io.IOException;
import java.util.Objects;

public abstract class ConfigObject extends StreamObject {
    public final BlockConfigEntry block;

    protected ConfigObject(BlockConfigEntry block) {
        this.block = Objects.requireNonNull(block, "block");
    }

    @Override
    public final void write(HOutput output) throws IOException {
        block.write(output);
    }

    @FunctionalInterface
    public interface Adapter<O extends ConfigObject> {
        O convert(BlockConfigEntry entry);
    }
}
