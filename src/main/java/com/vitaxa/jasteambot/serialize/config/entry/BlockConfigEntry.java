package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.helper.VerifyHelper;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;
import java.util.*;

public final class BlockConfigEntry extends ConfigEntry<Map<String, ConfigEntry<?>>> {
    public BlockConfigEntry(Map<String, ConfigEntry<?>> map, boolean ro, int cc) {
        super(map, ro, cc);
    }

    public BlockConfigEntry(int cc) {
        super(Collections.emptyMap(), false, cc);
    }

    public BlockConfigEntry(HInput input, boolean ro) throws IOException {
        super(readMap(input, ro), ro, 0);
    }

    private static Map<String, ConfigEntry<?>> readMap(HInput input, boolean ro) throws IOException {
        int entriesCount = input.readLength(0);
        Map<String, ConfigEntry<?>> map = MapHelper.newLinkedHashMapWithExpectedSize(entriesCount);
        for (int i = 0; i < entriesCount; i++) {
            String name = VerifyHelper.verifyIDName(input.readString(255));
            ConfigEntry<?> entry = readEntry(input, ro);

            // Try add entry to map
            VerifyHelper.putIfAbsent(map, name, entry, String.format("Duplicate config entry: '%s'", name));
        }
        return map;
    }

    @Override
    public Type getType() {
        return Type.BLOCK;
    }

    @Override
    public Map<String, ConfigEntry<?>> getValue() {
        Map<String, ConfigEntry<?>> value = super.getValue();
        return ro ? value : Collections.unmodifiableMap(value); // Already RO
    }

    @Override
    public void write(HOutput output) throws IOException {
        Set<Map.Entry<String, ConfigEntry<?>>> entries = getValue().entrySet();
        output.writeLength(entries.size(), 0);
        for (Map.Entry<String, ConfigEntry<?>> mapEntry : entries) {
            output.writeString(mapEntry.getKey(), 255);
            writeEntry(mapEntry.getValue(), output);
        }
    }

    @Override
    protected void uncheckedSetValue(Map<String, ConfigEntry<?>> value) {
        Map<String, ConfigEntry<?>> newValue = new LinkedHashMap<>(value);
        newValue.keySet().forEach(VerifyHelper::verifyIDName);

        // Call super method to actually set new value
        super.uncheckedSetValue(ro ? Collections.unmodifiableMap(newValue) : newValue);
    }

    public void clear() {
        super.getValue().clear();
    }

    public <E extends ConfigEntry<?>> E getEntry(String name, Class<E> clazz) {
        Map<String, ConfigEntry<?>> map = super.getValue();
        ConfigEntry<?> value = map.get(name);
        if (!clazz.isInstance(value)) {
            throw new NoSuchElementException(name);
        }
        return clazz.cast(value);
    }

    public <V, E extends ConfigEntry<V>> V getEntryValue(String name, Class<E> clazz) {
        return getEntry(name, clazz).getValue();
    }

    public boolean hasEntry(String name) {
        return getValue().containsKey(name);
    }

    public void remove(String name) {
        super.getValue().remove(name);
    }

    public void setEntry(String name, ConfigEntry<?> entry) {
        super.getValue().put(VerifyHelper.verifyIDName(name), entry);
    }
}
