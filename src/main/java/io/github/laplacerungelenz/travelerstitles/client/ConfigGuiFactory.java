package io.github.laplacerungelenz.travelerstitles.client;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;

import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import io.github.laplacerungelenz.travelerstitles.TravelersTitles;

public final class ConfigGuiFactory implements IModGuiFactory {

    @Override
    public void initialize(Minecraft minecraftInstance) {}

    @Override
    public Class<? extends GuiScreen> mainConfigGuiClass() {
        return Screen.class;
    }

    @Override
    public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Override
    public RuntimeOptionGuiHandler getHandlerFor(RuntimeOptionCategoryElement element) {
        return null;
    }

    public static final class Screen extends GuiConfig {

        public Screen(GuiScreen parent) {
            super(
                parent,
                new ConfigElement<>(ClientConfig.config.getCategory("general")).getChildElements(),
                TravelersTitles.ID,
                false,
                false,
                "Traveler's Titles GTNH");
        }
    }
}
