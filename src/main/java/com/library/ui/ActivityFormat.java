package com.library.ui;
import java.time.*;
import java.time.format.DateTimeFormatter;
/**
 * Formats activity actions and timestamps for display without changing stored values.
 *
 * @author Maaz
 */
public final class ActivityFormat {
    /**
     * Prevents instantiation of this static formatting utility.
     */
    private ActivityFormat() {}
    /**
     * Converts an offset timestamp to the requested zone using an English date format.
     * @param value raw display value or column value extractor
     * @param zone target display time zone
     * @return the formatted time, an em dash for blank input, or a fallback for malformed input
     */
    public static String timestamp(String value, ZoneId zone) {
        if (value == null || value.isBlank()) return "—";
        try { return OffsetDateTime.parse(value).atZoneSameInstant(zone)
            .format(DateTimeFormatter.ofPattern("dd MMM uuuu, HH:mm", java.util.Locale.ENGLISH)); }
        catch (java.time.format.DateTimeParseException invalid) { return "Time unavailable"; }
    }
    /**
     * Replaces underscores in action names for display.
     * @param value raw display value or column value extractor
     * @return the display action, or Unknown for blank input
     */
    public static String action(String value) {
        return value == null || value.isBlank() ? "Unknown" : value.replace('_', ' ');
    }
}
