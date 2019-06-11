package com.vitaxa.jasteambot.helper;

import com.vitaxa.steamauth.helper.Json;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import static java.lang.System.currentTimeMillis;

public final class CommonHelper {

    private CommonHelper() {
    }

    public static String low(String s) {
        return s.toLowerCase(Locale.US);
    }

    public static Thread newThread(String name, boolean daemon, Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.setDaemon(daemon);
        if (name != null) {
            thread.setName(name);
        }
        return thread;
    }

    public static int getThreadCount() {
        return Runtime.getRuntime().availableProcessors();
    }

    public static long getUnixTimestamp() {
        return currentTimeMillis() / 1000L;
    }

    public static boolean isNumeric(String value) {
        if (value == null) {
            return false;
        }
        int size = value.length();
        for (int i = 0; i < size; i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNumberOnly(String value) {
        return value.matches("[0-9]+");
    }

    public static boolean toBoolean(String num) {
        return num.equalsIgnoreCase("true") || num.equalsIgnoreCase("1");
    }

    public static boolean toBoolean(int num) {
        return num == 1;
    }

    public static boolean nullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    public static LocalDateTime convertStringToDateTime(String date) {
        Instant instant = Instant.parse(date);
        return LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
    }

    public static String durationBetweenDateTime(LocalDateTime startDate, LocalDateTime endDate) {
        LocalDateTime tempDateTime = LocalDateTime.from(startDate);
        long days = tempDateTime.until(endDate, ChronoUnit.DAYS);
        tempDateTime = tempDateTime.plusDays(days);
        long hours = tempDateTime.until(endDate, ChronoUnit.HOURS);
        tempDateTime = tempDateTime.plusHours(hours);
        long minutes = tempDateTime.until(endDate, ChronoUnit.MINUTES);
        tempDateTime = tempDateTime.plusMinutes(minutes);
        return String.format("%s day, %s hour, %s min", days, hours, minutes);
    }

    public static boolean isValidJSON(String json) {
        boolean valid = true;
        try {
            Json.getInstance().mapper().readTree(json);
        } catch (Exception e) {
            valid = false;
        }
        return valid;
    }
}
