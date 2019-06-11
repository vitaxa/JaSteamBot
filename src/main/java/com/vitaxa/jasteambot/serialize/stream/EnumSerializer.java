package com.vitaxa.jasteambot.serialize.stream;

import com.vitaxa.jasteambot.helper.MapHelper;
import com.vitaxa.jasteambot.helper.VerifyHelper;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

public final class EnumSerializer<E extends Enum<?> & EnumSerializer.Itf> {
    private final Map<Integer, E> map = MapHelper.newHashMapWithExpectedSize(16);

    public EnumSerializer(Class<E> clazz) {
        for (Field field : clazz.getFields()) {
            if (!field.isEnumConstant()) {
                continue;
            }

            // Add to map
            Itf itf;
            try {
                itf = (Itf) field.get(null);
            } catch (IllegalAccessException e) {
                throw new InternalError(e);
            }
            VerifyHelper.putIfAbsent(map, itf.getNumber(), clazz.cast(itf),
                    "Duplicate number for enum constant " + field.getName());
        }
    }

    public static void write(HOutput output, Itf itf) throws IOException {
        output.writeVarInt(itf.getNumber());
    }

    public E read(HInput input) throws IOException {
        int n = input.readVarInt();
        return VerifyHelper.getMapValue(map, n, "Unknown enum number: " + n);
    }

    @FunctionalInterface
    public interface Itf {
        int getNumber();
    }
}
