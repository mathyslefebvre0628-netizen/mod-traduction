package fr.mathyslefebvre.linguabridge.translation;

import java.util.concurrent.CompletableFuture;

public interface TranslationProvider {
    CompletableFuture<TranslationResult> translate(String text, String sourceLanguage, String targetLanguage);
}
