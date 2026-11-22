package com.library.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.Map;
import java.util.function.Function;

/** Resolves configuration without exposing values in diagnostics. */
public final class AppConfig {
    private final String url;
    private final String key;

    public AppConfig(Map<String, String> environment, Map<String, String> dotenv) {
        this(environment::get, dotenv::get);
    }

    private AppConfig(Function<String, String> environment, Function<String, String> dotenv) {
        url = resolve("SUPABASE_URL", environment, dotenv);
        key = resolve("SUPABASE_KEY", environment, dotenv);
    }

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

    private static String resolve(String name, Function<String, String> environment,
                                  Function<String, String> dotenv) {
        String value = environment.apply(name);
        if (value == null || value.isBlank()) value = dotenv.apply(name);
        if (value == null || value.isBlank())
            throw new IllegalStateException("Missing " + name + ". Set it in the OS environment or project-root .env.");
        return value.trim();
    }

    public String url() { return url; }
    public String key() { return key; }
}
