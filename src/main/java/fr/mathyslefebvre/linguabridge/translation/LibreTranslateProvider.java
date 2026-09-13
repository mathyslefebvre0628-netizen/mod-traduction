package fr.mathyslefebvre.linguabridge.translation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import fr.mathyslefebvre.linguabridge.config.ModConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public final class LibreTranslateProvider implements TranslationProvider {
    private final ModConfig config;
    private final HttpClient client;

    public LibreTranslateProvider(ModConfig config) {
        this.config = config;
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(config.timeoutMs))
                .build();
    }

    @Override
    public CompletableFuture<TranslationResult> translate(String text, String sourceLanguage, String targetLanguage) {
        JsonObject body = new JsonObject();
        body.addProperty("q", text);
        body.addProperty("source", sourceLanguage == null || sourceLanguage.isBlank() ? "auto" : sourceLanguage);
        body.addProperty("target", targetLanguage);
        body.addProperty("format", "text");
        if (config.apiKey != null && !config.apiKey.isBlank()) body.addProperty("api_key", config.apiKey);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(config.apiUrl.replaceAll("/$", "") + "/translate"))
                .timeout(Duration.ofMillis(config.timeoutMs))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() < 200 || response.statusCode() >= 300) {
                        throw new IllegalStateException("LibreTranslate HTTP " + response.statusCode());
                    }
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    String translated = json.get("translatedText").getAsString();
                    String detected = json.has("detectedLanguage") ? json.get("detectedLanguage").getAsString() : sourceLanguage;
                    return new TranslationResult(translated, detected);
                });
    }
}
