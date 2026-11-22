package com.library.ui;
import org.junit.jupiter.api.Test;
import java.time.ZoneId;
import static org.junit.jupiter.api.Assertions.*;
class ActivityFormatTest {
 @Test void convertsOffsetIntoLocalTime() {
  assertEquals("22 Nov 2026, 15:30",ActivityFormat.timestamp("2026-11-22T10:30:00Z",ZoneId.of("Asia/Karachi")));
 }
 @Test void handlesMissingAndMalformedValues() {
  assertEquals("—",ActivityFormat.timestamp(null,ZoneId.of("UTC")));
  assertEquals("Time unavailable",ActivityFormat.timestamp("invalid",ZoneId.of("UTC")));
  assertEquals("STATUS CHANGE",ActivityFormat.action("STATUS_CHANGE"));
  assertEquals("Unknown",ActivityFormat.action(null));
 }
}
