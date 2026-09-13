package fr.mathyslefebvre.linguabridge.gui;

import fr.mathyslefebvre.linguabridge.LinguaBridgeClient;
import fr.mathyslefebvre.linguabridge.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TranslatorConfigScreen extends Screen {
    private static final String[] LANGUAGES = {"auto", "fr", "en", "es", "de", "it", "pt", "nl", "pl", "ru", "ja", "ko", "zh"};
    private final Screen parent;
    private final ModConfig config;
    private EditBox apiUrl;
    private EditBox apiKey;
    private EditBox timeout;
    private EditBox testText;
    private Button enabledButton;
    private Button writingLanguageButton;
    private Button incomingLanguageButton;
    private Button autoDetectButton;
    private Button indicatorButton;
    private int writingIndex;
    private int incomingIndex;

    public TranslatorConfigScreen(Screen parent) {
        super(Component.translatable("screen.linguabridge.title"));
        this.parent = parent;
        this.config = LinguaBridgeClient.CONFIG;
        this.writingIndex = indexOf(config.outgoingLanguage);
        this.incomingIndex = indexOf(config.incomingLanguage);
    }

    @Override
    protected void init() {
        int left = this.width / 2 - 150;
        int top = 30;

        enabledButton = addRenderableWidget(Button.builder(label("screen.linguabridge.translation", config.enabled), b -> {
            config.enabled = !config.enabled;
            refreshLabels();
        }).bounds(left, top, 300, 20).build());

        writingLanguageButton = addRenderableWidget(Button.builder(languageButtonLabel("screen.linguabridge.writing", config.outgoingLanguage), b -> {
            writingIndex = (writingIndex + 1) % LANGUAGES.length;
            config.outgoingLanguage = LANGUAGES[writingIndex];
            refreshLabels();
        }).bounds(left, top + 25, 300, 20).build());

        incomingLanguageButton = addRenderableWidget(Button.builder(languageButtonLabel("screen.linguabridge.incoming", config.incomingLanguage), b -> {
            incomingIndex = (incomingIndex + 1) % LANGUAGES.length;
            config.incomingLanguage = LANGUAGES[incomingIndex];
            refreshLabels();
        }).bounds(left, top + 50, 300, 20).build());

        autoDetectButton = addRenderableWidget(Button.builder(label("screen.linguabridge.auto_detect", config.autoDetectIncoming), b -> {
            config.autoDetectIncoming = !config.autoDetectIncoming;
            refreshLabels();
        }).bounds(left, top + 75, 300, 20).build());

        indicatorButton = addRenderableWidget(Button.builder(label("screen.linguabridge.indicator", config.showTranslationIndicator), b -> {
            config.showTranslationIndicator = !config.showTranslationIndicator;
            refreshLabels();
        }).bounds(left, top + 100, 300, 20).build());

        apiUrl = addRenderableWidget(new EditBox(this.font, left, top + 140, 300, 20, Component.translatable("screen.linguabridge.api_url")));
        apiUrl.setValue(config.apiUrl);

        apiKey = addRenderableWidget(new EditBox(this.font, left, top + 165, 300, 20, Component.translatable("screen.linguabridge.api_key")));
        apiKey.setValue(config.apiKey);
        apiKey.setSuggestion("API key (optional for some servers)");

        timeout = addRenderableWidget(new EditBox(this.font, left, top + 190, 300, 20, Component.translatable("screen.linguabridge.timeout")));
        timeout.setValue(Integer.toString(config.timeoutMs));

        testText = addRenderableWidget(new EditBox(this.font, left, top + 215, 300, 20, Component.translatable("screen.linguabridge.test_text")));
        testText.setValue("Bonjour, comment allez-vous ?");

        addRenderableWidget(Button.builder(Component.translatable("screen.linguabridge.test"), b -> testTranslation())
                .bounds(left, top + 240, 145, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("screen.linguabridge.save"), b -> saveAndClose())
                .bounds(left + 155, top + 240, 145, 20).build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_CANCEL, b -> {
            config.load();
            Minecraft.getInstance().setScreen(parent);
        }).bounds(left, top + 265, 300, 20).build());
    }

    private void testTranslation() {
        saveFields();
        LinguaBridgeClient.TRANSLATIONS.translate(testText.getValue(), config.outgoingLanguage, "en")
                .whenComplete((result, error) -> Minecraft.getInstance().execute(() -> {
                    if (error != null || result == null) {
                        Minecraft.getInstance().gui.setOverlayMessage(Component.translatable("screen.linguabridge.test_failed"), false);
                    } else {
                        Minecraft.getInstance().gui.setOverlayMessage(Component.literal(result.text()), false);
                    }
                }));
    }

    private void saveAndClose() {
        saveFields();
        config.save();
        Minecraft.getInstance().setScreen(parent);
    }

    private void saveFields() {
        config.apiUrl = apiUrl.getValue().trim();
        config.apiKey = apiKey.getValue();
        try {
            config.timeoutMs = Math.max(500, Math.min(30000, Integer.parseInt(timeout.getValue().trim())));
        } catch (NumberFormatException ignored) {
            config.timeoutMs = 4500;
        }
    }

    private void refreshLabels() {
        enabledButton.setMessage(label("screen.linguabridge.translation", config.enabled));
        writingLanguageButton.setMessage(languageButtonLabel("screen.linguabridge.writing", config.outgoingLanguage));
        incomingLanguageButton.setMessage(languageButtonLabel("screen.linguabridge.incoming", config.incomingLanguage));
        autoDetectButton.setMessage(label("screen.linguabridge.auto_detect", config.autoDetectIncoming));
        indicatorButton.setMessage(label("screen.linguabridge.indicator", config.showTranslationIndicator));
    }

    private static Component label(String key, boolean value) {
        return Component.translatable(key).append(Component.literal(": " + (value ? "ON" : "OFF")));
    }

    private static Component languageButtonLabel(String key, String code) {
        return Component.translatable(key).append(Component.literal(": " + languageName(code)));
    }

    private static String languageName(String code) {
        return switch (code) {
            case "auto" -> "Auto";
            case "fr" -> "Français";
            case "en" -> "English";
            case "es" -> "Español";
            case "de" -> "Deutsch";
            case "it" -> "Italiano";
            case "pt" -> "Português";
            case "nl" -> "Nederlands";
            case "pl" -> "Polski";
            case "ru" -> "Русский";
            case "ja" -> "日本語";
            case "ko" -> "한국어";
            case "zh" -> "中文";
            default -> code;
        };
    }

    private static int indexOf(String code) {
        for (int i = 0; i < LANGUAGES.length; i++) if (LANGUAGES[i].equalsIgnoreCase(code)) return i;
        return 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics, mouseX, mouseY, delta);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        saveAndClose();
    }
}
