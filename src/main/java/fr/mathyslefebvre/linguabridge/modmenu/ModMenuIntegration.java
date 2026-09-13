package fr.mathyslefebvre.linguabridge.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import fr.mathyslefebvre.linguabridge.gui.TranslatorConfigScreen;
import net.minecraft.client.gui.screens.Screen;

public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TranslatorConfigScreen::new;
    }
}
