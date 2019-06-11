package com.vitaxa.jasteambot.helper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * From guava library (https://github.com/google/guava/blob/master/guava/src/com/google/common/collect/Maps.java)
 */
public final class MapHelper {

    public static final int MAX_POWER_OF_TWO = 1 << (Integer.SIZE - 2);

    private MapHelper() {
    }

    /**
     * Creates a {@code HashMap} instance, with a high enough "initial capacity" that it <i>should</i>
     * hold {@code expectedSize} elements without growth. This behavior cannot be broadly guaranteed,
     * but it is observed to be true for OpenJDK 1.7. It also can't be guaranteed that the method
     * isn't inadvertently <i>oversizing</i> the returned map.
     *
     * @param expectedSize the number of entries you expect to add to the returned map
     * @return a new, empty {@code HashMap} with enough capacity to hold {@code expectedSize} entries
     * without resizing
     * @throws IllegalArgumentException if {@code expectedSize} is negative
     */
    public static <K, V> HashMap<K, V> newHashMapWithExpectedSize(int expectedSize) {
        return new HashMap<>(capacity(expectedSize));
    }

    /**
     * Creates a {@code LinkedHashMap} instance, with a high enough "initial capacity" that it
     * <i>should</i> hold {@code expectedSize} elements without growth. This behavior cannot be
     * broadly guaranteed, but it is observed to be true for OpenJDK 1.7. It also can't be guaranteed
     * that the method isn't inadvertently <i>oversizing</i> the returned map.
     *
     * @param expectedSize the number of entries you expect to add to the returned map
     * @return a new, empty {@code LinkedHashMap} with enough capacity to hold {@code expectedSize}
     * entries without resizing
     * @throws IllegalArgumentException if {@code expectedSize} is negative
     */
    public static <K, V> LinkedHashMap<K, V> newLinkedHashMapWithExpectedSize(int expectedSize) {
        return new LinkedHashMap<>(capacity(expectedSize));
    }

    /**
     * Returns a capacity that is sufficient to keep the map from being resized as long as it grows no
     * larger than expectedSize and the load factor is ≥ its default (0.75).
     */
    static int capacity(int expectedSize) {
        if (expectedSize < 3) {
            VerifyHelper.verifyInt(expectedSize, VerifyHelper.NOT_NEGATIVE, "expectedSize");
            return expectedSize + 1;
        }
        if (expectedSize < MAX_POWER_OF_TWO) {
            // This is the calculation used in JDK8 to resize when a putAll
            // happens; it seems to be the most conservative calculation we
            // can make.  0.75 is the default load factor.
            return (int) ((float) expectedSize / 0.75F + 1.0F);
        }
        return Integer.MAX_VALUE; // any large value
    }

    /**
     * Delegates to {@link Map#get}. Returns {@code null} on {@code ClassCastException} and {@code
     * NullPointerException}.
     */
    public static <V> V safeGet(Map<?, V> map, Object key) {
        if (map == null) {
            throw new NullPointerException();
        }
        try {
            return map.get(key);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

    /**
     * Delegates to {@link Map#containsKey}. Returns {@code false} on {@code ClassCastException} and
     * {@code NullPointerException}.
     */
    public static boolean safeContainsKey(Map<?, ?> map, Object key) {
        if (map == null) {
            throw new NullPointerException();
        }
        try {
            return map.containsKey(key);
        } catch (ClassCastException | NullPointerException e) {
            return false;
        }
    }

    /**
     * Delegates to {@link Map#remove}. Returns {@code null} on {@code ClassCastException} and {@code
     * NullPointerException}.
     */
    public static <V> V safeRemove(Map<?, V> map, Object key) {
        if (map == null) {
            throw new NullPointerException();
        }
        try {
            return map.remove(key);
        } catch (ClassCastException | NullPointerException e) {
            return null;
        }
    }

}
