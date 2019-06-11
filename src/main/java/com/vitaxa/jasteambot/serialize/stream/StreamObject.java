package com.vitaxa.jasteambot.serialize.stream;

import com.vitaxa.jasteambot.helper.IOHelper;
import com.vitaxa.jasteambot.serialize.HInput;
import com.vitaxa.jasteambot.serialize.HOutput;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract class StreamObject {

    public abstract void write(HOutput output) throws IOException;

    public final byte[] write() throws IOException {
        try (ByteArrayOutputStream array = IOHelper.newByteArrayOutput()) {
            try (HOutput output = new HOutput(array)) {
                write(output);
            }
            return array.toByteArray();
        }
    }

    @FunctionalInterface
    public interface Adapter<O extends StreamObject> {
        O convert(HInput input) throws IOException;
    }
}
