package fr.mathyslefebvre.linguabridge.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("linguabridge.json");

    public boolean enabled = true;
    public String outgoingLanguage = "fr";
    public String incomingLanguage = "fr";
    public boolean autoDetectIncoming = true;
    public boolean showTranslationIndicator = true;
    public String provider = "libretranslate";
    public String apiUrl = "https://libretranslate.com";
    public String apiKey = "";
    public int timeoutMs = 4500;
    public int maxConcurrentRequests = 2;
    public int cacheSize = 256;

    public static ModConfig load() {
        try {
            if (Files.exists(FILE)) {
                try (Reader reader = Files.newBufferedReader(FILE)) {
                    ModConfig config = GSON.fromJson(reader, ModConfig.class);
                    if (config != null) return config;
                }
            }
        } catch (Exception ignored) {
        }
        ModConfig config = new ModConfig();
        config.save();
        return config;
    }

    public void save() {
        try {
            Files.createDirectories(FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(FILE)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception ignored) {
        }
    }
}
