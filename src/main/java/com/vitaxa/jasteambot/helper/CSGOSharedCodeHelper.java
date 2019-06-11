package com.vitaxa.jasteambot.helper;

import com.vitaxa.jasteambot.model.ShareCodeStruct;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CSGOSharedCodeHelper {

    private final static String DICTIONARY = "ABCDEFGHJKLMNOPQRSTUVWXYZabcdefhijkmnopqrstuvwxyz23456789";

    private CSGOSharedCodeHelper() {
    }

    public static String encode(Long matchId, Long reservationId, Integer tvPort) {
        final byte[] matchIdBytes = longToBytes(matchId);
        reverse(matchIdBytes);
        final byte[] reservationIdBytes = longToBytes(reservationId);
        reverse(reservationIdBytes);
        final short tvPortShort = (short) (tvPort & ((1 << 16) - 1));
        final byte[] tvBytes = ByteBuffer.allocate(Short.BYTES).putShort(tvPortShort).array();
        reverse(tvBytes);

        byte[] bytes = new byte[matchIdBytes.length + reservationIdBytes.length + tvBytes.length + 1];

        System.arraycopy(new byte[]{0}, 0, bytes, 0, 1);
        System.arraycopy(matchIdBytes, 0, bytes, 1, matchIdBytes.length);
        System.arraycopy(reservationIdBytes, 0, bytes, 1 + matchIdBytes.length, reservationIdBytes.length);
        System.arraycopy(tvBytes, 0, bytes, 1 + matchIdBytes.length + reservationIdBytes.length, tvBytes.length);

        BigInteger big = new BigInteger(bytes);

        char[] charArray = DICTIONARY.toCharArray();
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 25; i++) {
            final BigInteger rem = big.remainder(BigInteger.valueOf(charArray.length));
            sb.append(charArray[(int) rem.intValue()]);
            big = big.divide(BigInteger.valueOf(charArray.length));
        }

        final String c = sb.toString();

        return String.format("CSGO-%s-%s-%s-%s-%s", c.substring(0, 5), c.substring(5, 10), c.substring(10, 15), c.substring(15, 20), c.substring(20, 25));
    }

    public static ShareCodeStruct decode(String sharedCode) {
        final Pattern pattern = Pattern.compile(String.format("^(CSGO)?(-?[%s]{5}){5}$", DICTIONARY));
        final Matcher matcher = pattern.matcher(sharedCode);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Invalid share code");
        }
        sharedCode = sharedCode.replaceAll("CSGO-|-", "");

        BigInteger big = BigInteger.ZERO;
        final char[] charArray = sharedCode.toCharArray();
        reverse(charArray);
        for (char c : charArray) {
            big = big.multiply(BigInteger.valueOf(DICTIONARY.length())).add(BigInteger.valueOf(DICTIONARY.indexOf(c)));
        }

        final byte[] matchIdBytes = new byte[Long.BYTES];
        final byte[] outcomeIdBytes = new byte[Long.BYTES];
        final byte[] tvPortIdBytes = new byte[Integer.BYTES];

        byte[] all = big.toByteArray();

        if (all.length == (Long.BYTES + Long.BYTES + Short.BYTES)) {
            all = concat(new byte[]{0}, all);
        }
        System.arraycopy(all, 1, matchIdBytes, 0, Long.BYTES);
        System.arraycopy(all, 1 + Long.BYTES, outcomeIdBytes, 0, Long.BYTES);
        System.arraycopy(all, 1 + 2 * Long.BYTES, tvPortIdBytes, 0, Short.BYTES);

        long matchId = bytesToLong(matchIdBytes);
        long outComeId = bytesToLong(outcomeIdBytes);
        int tokenId = bytesToInt(tvPortIdBytes);

        return new ShareCodeStruct(matchId, outComeId, tokenId);
    }

    private static byte[] concat(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte[] result = new byte[length];
        int pos = 0;
        for (byte[] array : arrays) {
            System.arraycopy(array, 0, result, pos, array.length);
            pos += array.length;
        }
        return result;
    }

    private static void reverse(byte[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        byte tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private static void reverse(char[] array) {
        if (array == null) {
            return;
        }
        int i = 0;
        int j = array.length - 1;
        char tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    private static byte[] longToBytes(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }

    private static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getLong();
    }

    private static int bytesToInt(byte[] bytes) {
        final ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

}
