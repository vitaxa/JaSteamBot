package com.vitaxa.jasteambot.serialize.config.entry;

import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public final class ListConfigEntry extends ConfigEntry<List<ConfigEntry<?>>> {
    public ListConfigEntry(List<ConfigEntry<?>> value, boolean ro, int cc) {
        super(value, ro, cc);
    }

    public ListConfigEntry(HInput input, boolean ro) throws IOException {
        super(readList(input, ro), ro, 0);
    }

    private static List<ConfigEntry<?>> readList(HInput input, boolean ro) throws IOException {
        int elementsCount = input.readLength(0);
        List<ConfigEntry<?>> list = new ArrayList<>(elementsCount);
        for (int i = 0; i < elementsCount; i++) {
            list.add(readEntry(input, ro));
        }
        return list;
    }

    @Override
    public Type getType() {
        return Type.LIST;
    }

    @Override
    public void write(HOutput output) throws IOException {
        List<ConfigEntry<?>> value = getValue();
        output.writeLength(value.size(), 0);
        for (ConfigEntry<?> element : value) {
            writeEntry(element, output);
        }
    }

    @Override
    protected void uncheckedSetValue(List<ConfigEntry<?>> value) {
        List<ConfigEntry<?>> list = new ArrayList<>(value);
        super.uncheckedSetValue(ro ? Collections.unmodifiableList(list) : list);
    }

    public <V, E extends ConfigEntry<V>> Stream<V> stream(Class<E> clazz) {
        return getValue().stream().map(clazz::cast).map(ConfigEntry::getValue);
    }

    public void verifyOfType(Type type) {
        if (getValue().stream().anyMatch(e -> e.getType() != type)) {
            throw new IllegalArgumentException("List type mismatch: " + type.name());
        }
    }
}
