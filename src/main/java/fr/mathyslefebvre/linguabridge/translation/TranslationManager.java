package fr.mathyslefebvre.linguabridge.translation;

import fr.mathyslefebvre.linguabridge.config.ModConfig;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

public final class TranslationManager {
    private final ModConfig config;
    private final TranslationProvider provider;
    private final ExecutorService executor = Executors.newFixedThreadPool(2, r -> {
        Thread thread = new Thread(r, "LinguaBridge-translation");
        thread.setDaemon(true);
        return thread;
    });
    private final Semaphore permits;
    private final Map<String, TranslationResult> cache = new LinkedHashMap<>(64, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, TranslationResult> eldest) {
            return size() > Math.max(16, config.cacheSize);
        }
    };

    public TranslationManager(ModConfig config) {
        this.config = config;
        this.provider = new LibreTranslateProvider(config);
        this.permits = new Semaphore(Math.max(1, Math.min(8, config.maxConcurrentRequests)));
    }

    public CompletableFuture<TranslationResult> translate(String text, String source, String target) {
        if (text == null || text.isBlank() || source == null || target == null || source.equalsIgnoreCase(target)) {
            return CompletableFuture.completedFuture(new TranslationResult(text, source));
        }

        String key = source + "\u0000" + target + "\u0000" + text;
        synchronized (cache) {
            TranslationResult cached = cache.get(key);
            if (cached != null) return CompletableFuture.completedFuture(cached);
        }

        return CompletableFuture.supplyAsync(() -> {
            boolean acquired = false;
            try {
                permits.acquire();
                acquired = true;
                return provider.translate(text, source, target).join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Translation interrupted", e);
            } finally {
                if (acquired) permits.release();
            }
        }, executor).thenApply(result -> {
            synchronized (cache) {
                cache.put(key, result);
            }
            return result;
        });
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
