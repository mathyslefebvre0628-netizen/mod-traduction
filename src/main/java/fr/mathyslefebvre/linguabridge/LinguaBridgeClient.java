package fr.mathyslefebvre.linguabridge;

import fr.mathyslefebvre.linguabridge.config.ModConfig;
import fr.mathyslefebvre.linguabridge.translation.TranslationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class LinguaBridgeClient implements ClientModInitializer {
    public static final String MOD_ID = "linguabridge";
    public static final ModConfig CONFIG = ModConfig.load();
    public static TranslationManager TRANSLATIONS;
    private static KeyMapping toggleKey;
    private static boolean forwardingTranslatedMessage;

    @Override
    public void onInitializeClient() {
        TRANSLATIONS = new TranslationManager(CONFIG);

        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.linguabridge.toggle",
                GLFW.GLFW_KEY_F8,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                CONFIG.enabled = !CONFIG.enabled;
                CONFIG.save();
                if (client.player != null) {
                    client.player.displayClientMessage(
                            net.minecraft.network.chat.Component.literal("LinguaBridge: " + (CONFIG.enabled ? "ON" : "OFF")),
                            true
                    );
                }
            }
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (forwardingTranslatedMessage || !CONFIG.enabled || message.startsWith("/")) {
                return true;
            }
            if (message.isBlank()) return true;

            forwardingTranslatedMessage = true;
            TRANSLATIONS.translate(message, CONFIG.outgoingLanguage, "en").whenComplete((translated, error) -> {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> {
                    try {
                        if (error == null && translated != null && !translated.isBlank()) {
                            client.player.connection.sendChat(translated);
                        } else if (client.player != null) {
                            client.player.displayClientMessage(ComponentUtil.error("Translation failed; original message was not sent."), true);
                        }
                    } finally {
                        forwardingTranslatedMessage = false;
                    }
                });
            });
            return false;
        });
    }
}
