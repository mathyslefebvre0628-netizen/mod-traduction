package fr.mathyslefebvre.linguabridge;

import fr.mathyslefebvre.linguabridge.config.ModConfig;
import fr.mathyslefebvre.linguabridge.translation.TranslationManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
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
                "key.linguabridge.toggle", GLFW.GLFW_KEY_F8, KeyMapping.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                CONFIG.enabled = !CONFIG.enabled;
                CONFIG.save();
                client.gui.setOverlayMessage(Component.literal("LinguaBridge: " + (CONFIG.enabled ? "ON" : "OFF")), false);
            }
        });

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (forwardingTranslatedMessage || !CONFIG.enabled || message.startsWith("/") || message.isBlank()) return true;

            forwardingTranslatedMessage = true;
            TRANSLATIONS.translate(message, CONFIG.outgoingLanguage, "en").whenComplete((result, error) -> {
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> {
                    try {
                        if (error == null && result != null && result.text() != null && !result.text().isBlank() && client.player != null) {
                            client.player.connection.sendChat(result.text());
                        } else if (client.player != null) {
                            client.gui.setOverlayMessage(Component.literal("LinguaBridge: translation failed"), false);
                        }
                    } finally {
                        forwardingTranslatedMessage = false;
                    }
                });
            });
            return false;
        });

        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!CONFIG.enabled) return true;
            String original = message.getString();
            if (original.isBlank() || original.startsWith("/") || isTrivial(original)) return true;

            TRANSLATIONS.translate(original, "auto", CONFIG.incomingLanguage).whenComplete((result, error) -> {
                if (error != null || result == null || result.text() == null || result.text().isBlank()) return;
                Minecraft client = Minecraft.getInstance();
                client.execute(() -> {
                    if (client.player == null) return;

                    Component displayed = Component.literal(result.text());
                    if (sender != null) {
                        String senderName = sender.getName();
                        if (senderName != null && !senderName.isBlank() && !original.startsWith(senderName)) {
                            displayed = Component.literal(senderName + ": ").append(displayed);
                        }
                    }
                    if (CONFIG.showTranslationIndicator) {
                        displayed = Component.literal("[" + CONFIG.incomingLanguage.toUpperCase() + "] ").append(displayed);
                    }
                    client.gui.chatListener().handleSystemMessage(displayed, false);
                });
            });
            return false;
        });
    }

    private static boolean isTrivial(String text) {
        String s = text.trim().toLowerCase();
        return s.equals("gg") || s.equals("lol") || s.equals("ok") || s.equals("xd")
                || s.equals("?") || s.equals("!") || s.equals("yes") || s.equals("no");
    }
}
