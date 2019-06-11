package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;
import com.vitaxa.jasteambot.serialize.stream.EnumSerializer;
import com.vitaxa.jasteambot.serialize.stream.StreamObject;

import java.io.IOException;
import java.util.Objects;

public abstract class ConfigEntry<V> extends StreamObject {
    public final boolean ro;
    private final String[] comments;
    private V value;

    protected ConfigEntry(V value, boolean ro, int cc) {
        this.ro = ro;
        comments = new String[cc];
        uncheckedSetValue(value);
    }

    protected static ConfigEntry<?> readEntry(HInput input, boolean ro) throws IOException {
        Type type = Type.read(input);
        switch (type) {
            case BOOLEAN:
                return new BooleanConfigEntry(input, ro);
            case INTEGER:
                return new IntegerConfigEntry(input, ro);
            case STRING:
                return new StringConfigEntry(input, ro);
            case LIST:
                return new ListConfigEntry(input, ro);
            case BLOCK:
                return new BlockConfigEntry(input, ro);
            default:
                throw new AssertionError("Unsupported config entry type: " + type.name());
        }
    }

    protected static void writeEntry(ConfigEntry<?> entry, HOutput output) throws IOException {
        EnumSerializer.write(output, entry.getType());
        entry.write(output);
    }

    public final String getComment(int i) {
        if (i < 0) {
            i += comments.length;
        }
        return i >= comments.length ? null : comments[i];
    }

    public abstract Type getType();

    public V getValue() {
        return value;
    }

    public final void setValue(V value) {
        ensureWritable();
        uncheckedSetValue(value);
    }

    public final void setComment(int i, String comment) {
        comments[i] = comment;
    }

    protected final void ensureWritable() {
        if (ro) {
            throw new UnsupportedOperationException("Read-only");
        }
    }

    protected void uncheckedSetValue(V value) {
        this.value = Objects.requireNonNull(value, "value");
    }

    public enum Type implements EnumSerializer.Itf {
        BLOCK(1), BOOLEAN(2), INTEGER(3), STRING(4), LIST(5), LONG(6);
        private static final EnumSerializer<Type> SERIALIZER = new EnumSerializer<>(Type.class);
        private final int n;

        Type(int n) {
            this.n = n;
        }

        public static Type read(HInput input) throws IOException {
            return SERIALIZER.read(input);
        }

        @Override
        public int getNumber() {
            return n;
        }
    }
}
