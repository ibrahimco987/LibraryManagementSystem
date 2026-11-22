package com.library.config;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    @Test void environmentWinsAndIsTrimmed() {
        AppConfig config = new AppConfig(Map.of("SUPABASE_URL", " https://example.invalid ", "SUPABASE_KEY", " os-placeholder "),
                Map.of("SUPABASE_URL", "ignored", "SUPABASE_KEY", "ignored"));
        assertEquals("https://example.invalid", config.url());
        assertEquals("os-placeholder", config.key());
    }
    @Test void blankEnvironmentFallsBackPerSetting() {
        AppConfig config = new AppConfig(Map.of("SUPABASE_URL", "https://example.invalid", "SUPABASE_KEY", "  "),
                Map.of("SUPABASE_KEY", " file-placeholder "));
        assertEquals("https://example.invalid", config.url());
        assertEquals("file-placeholder", config.key());
    }
    @Test void absentFileWorksWithEnvironment() {
        assertDoesNotThrow(() -> new AppConfig(Map.of("SUPABASE_URL", "url", "SUPABASE_KEY", "placeholder"), Map.of()));
    }
    @Test void absentEnvironmentUsesFile() {
        assertEquals("placeholder", new AppConfig(Map.of(), Map.of("SUPABASE_URL", "url", "SUPABASE_KEY", "placeholder")).key());
    }
    @Test void missingUrlIsDescriptive() {
        assertTrue(assertThrows(IllegalStateException.class, () -> new AppConfig(Map.of(), Map.of())).getMessage().contains("SUPABASE_URL"));
    }
    @Test void missingKeyDoesNotExposeValues() {
        String message = assertThrows(IllegalStateException.class,
                () -> new AppConfig(Map.of("SUPABASE_URL", "private-value"), Map.of("SUPABASE_KEY", " "))).getMessage();
        assertTrue(message.contains("SUPABASE_KEY"));
        assertFalse(message.contains("private-value"));
    }
}
