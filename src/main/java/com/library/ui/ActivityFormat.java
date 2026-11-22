package com.library.ui;
import java.time.*;
import java.time.format.DateTimeFormatter;
public final class ActivityFormat {
    private ActivityFormat() {}
    public static String timestamp(String value, ZoneId zone) {
        if (value == null || value.isBlank()) return "—";
        try { return OffsetDateTime.parse(value).atZoneSameInstant(zone)
            .format(DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm", java.util.Locale.ENGLISH)); }
        catch (java.time.format.DateTimeParseException invalid) { return "Time unavailable"; }
    }
    public static String action(String value) {
        return value == null || value.isBlank() ? "Unknown" : value.replace('_', ' ');
    }
}
