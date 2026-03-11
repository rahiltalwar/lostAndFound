package lostandfound;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Loads configuration from (in order of priority):
 *   1. config.properties file next to the JAR / in the working directory
 *   2. Environment variables
 *   3. Built-in defaults (Ollama + llava)
 *
 * Supported keys in config.properties:
 *
 *   ai.provider=ollama          # "ollama" or "claude"
 *
 *   # ── Ollama settings ──
 *   ollama.url=http://localhost:11434
 *   ollama.model=llava          # any vision-capable model pulled in Ollama
 *
 *   # ── Claude / Anthropic settings ──
 *   claude.apiKey=sk-ant-...
 *   claude.model=claude-opus-4-6
 *
 *   # ── Server ──
 *   server.port=8080
 */
public class AppConfig {

    public static String ollamaUrl    = "http://localhost:11434";
    public static String ollamaModel  = "llama3.2-vision";
    public static double temperature  = 0.1;

    public static int    serverPort   = 8080;

    // ── Load ────────────────────────────────────────────────────────────────

    public static void load() {
        Properties props = new Properties();

        Path configFile = Path.of("config.properties");
        if (Files.exists(configFile)) {
            try (InputStream in = Files.newInputStream(configFile)) {
                props.load(in);
                System.out.println("📄 Loaded config from: " + configFile.toAbsolutePath());
            } catch (IOException e) {
                System.err.println("⚠️  Could not read config.properties: " + e.getMessage());
            }
        } else {
            System.out.println("ℹ️  No config.properties found — using defaults (Ollama + llava:13b)");
            writeDefaultConfig(configFile);
        }

        // Override with env vars where set
        mergeEnv(props, "ollama.url",         "OLLAMA_URL");
        mergeEnv(props, "ollama.model",       "OLLAMA_MODEL");
        mergeEnv(props, "ollama.temperature", "OLLAMA_TEMPERATURE");
        mergeEnv(props, "server.port",        "SERVER_PORT");

        ollamaUrl   = props.getProperty("ollama.url",   ollamaUrl).trim();
        ollamaModel = props.getProperty("ollama.model", ollamaModel).trim();

        try { temperature = Double.parseDouble(props.getProperty("ollama.temperature", "0.1").trim()); }
        catch (NumberFormatException ignored) {}

        try { serverPort = Integer.parseInt(props.getProperty("server.port", "8080").trim()); }
        catch (NumberFormatException ignored) {}

        printConfig();
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private static void mergeEnv(Properties props, String key, String envVar) {
        String val = System.getenv(envVar);
        if (val != null && !val.isBlank()) props.setProperty(key, val);
    }

    private static void printConfig() {
        System.out.println("┌─────────────────────────────────────────────┐");
        System.out.println("│  AI Configuration                           │");
        System.out.println("├─────────────────────────────────────────────┤");
        System.out.println("│  Provider : Ollama (local)                  │");
        System.out.printf( "│  URL      : %-32s│%n", ollamaUrl);
        System.out.printf( "│  Model    : %-32s│%n", ollamaModel);
        System.out.printf( "│  Temperature: %-30s│%n", temperature);
        System.out.println("└─────────────────────────────────────────────┘");
    }

    private static void writeDefaultConfig(Path path) {
        String content = """
# ─────────────────────────────────────────────────────────────────
# Lost & Found AI Agent — Configuration
# Edit this file then restart the app to apply changes.
# ─────────────────────────────────────────────────────────────────

# ── Ollama settings ──────────────────────────────────────────────
# Make sure Ollama is running:  https://ollama.com
# Pull the model first:         ollama pull llama3.2-vision
ollama.url=http://localhost:11434
ollama.model=llama3.2-vision

# Temperature: 0.0 = fully deterministic, 1.0 = creative/random
# Keep low (0.1) to reduce hallucinations in search results
ollama.temperature=0.1

# Other models to try (run  ollama pull <name>  first):
#   ollama.model=llava          (7B — faster, less RAM needed)
#   ollama.model=moondream      (very fast, lightweight)
#   ollama.model=bakllava       (alternative vision model)
#   ollama.model=llama3         (text-only — great search, no image analysis)
#   ollama.model=mistral        (text-only)

# ── Server ──────────────────────────────────────────────────────
server.port=8080
""";
        try {
            Files.writeString(path, content);
            System.out.println("✅ Created default config.properties — edit it to change the model.");
        } catch (IOException e) {
            System.err.println("Could not write default config: " + e.getMessage());
        }
    }
}
