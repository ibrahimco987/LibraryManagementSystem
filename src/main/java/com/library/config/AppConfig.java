package com.library.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Map;
import java.util.function.Function;

/** Resolves configuration without exposing values in diagnostics.
 * @author Maaz
 */
public final class AppConfig {
    private final String url;
    private final String key;

    /**
     * Creates a resolved configuration snapshot from the supplied setting sources.
     * @param environment injected OS setting values
     * @param dotenv injected file setting values or provider
     */
    public AppConfig(Map<String, String> environment, Map<String, String> dotenv) {
        this(environment::get, dotenv::get);
    }

    /**
     * Creates a resolved configuration snapshot from the supplied setting sources.
     * @param environment injected OS setting values
     * @param dotenv injected file setting values or provider
     */
    private AppConfig(Function<String, String> environment, Function<String, String> dotenv) {
        url = resolve("SUPABASE_URL", environment, dotenv);
        key = resolve("SUPABASE_KEY", environment, dotenv);
    }

    /**
     * Loads configuration lazily from OS settings and an optional working-directory .env file.
     * @return the resolved application configuration
     * @throws IllegalStateException if a required setting is unavailable or the file cannot be read
     */
    public static AppConfig load() {
        // Lazily parse the file only when an OS setting needs a fallback.
        return new AppConfig(System::getenv, new Function<>() {
            private Dotenv dotenv;
            public String apply(String name) {
                try {
                    if (dotenv == null) dotenv = Dotenv.configure().directory(".").ignoreIfMissing().load();
                    return dotenv.entries(Dotenv.Filter.DECLARED_IN_ENV_FILE).stream()
                            .filter(entry -> entry.getKey().equals(name))
                            .map(entry -> entry.getValue()).findFirst().orElse(null);
                } catch (RuntimeException exception) {
                    throw new IllegalStateException("Cannot read .env. Check its format in the project root.");
                }
            }
        });
    }

    /**
     * Resolves a nonblank OS value before consulting the dotenv provider.
     * @param name required configuration setting name
     * @param environment injected OS setting values
     * @param dotenv injected file setting values or provider
     * @return the trimmed setting
     * @throws IllegalStateException if a required setting is unavailable or the file cannot be read
     */
    private static String resolve(String name, Function<String, String> environment,
                                  Function<String, String> dotenv) {
        String value = environment.apply(name);
        if (value == null || value.isBlank()) value = dotenv.apply(name);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Missing " + name + ". Set it in the OS environment or project-root .env.");
        return value.trim();
    }

    /**
     * Returns the resolved Supabase project URL.
     * @return the trimmed project URL
     */
    public String url() { return url; }
    /**
     * Returns the resolved client key for HTTP headers. Do not log this value.
     * @return the trimmed publishable or anon client key
     */
    public String key() { return key; }
}
