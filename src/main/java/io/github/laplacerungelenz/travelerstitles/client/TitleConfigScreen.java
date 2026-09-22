package io.github.laplacerungelenz.travelerstitles.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.common.config.ConfigElement;

import cpw.mods.fml.client.config.GuiConfig;
import io.github.laplacerungelenz.travelerstitles.TravelersTitles;

/** Common entry point for the mod list and the in-world pause menu. */
public final class TitleConfigScreen extends GuiScreen {

    private final GuiScreen parent;

    public TitleConfigScreen(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        int top = 40;
        for (int i = 0; i < 3; i++) {
            String category = new String[] { "general", "dimension", "biome" }[i];
            buttonList
                .add(new GuiButton(i, width / 2 - 100, top + i * 24, 200, 20, I18n.format("ttgtnh.gui." + category)));
        }
        buttonList
            .add(new GuiButton(3, width / 2 - 155, top + 76, 150, 20, I18n.format("ttgtnh.gui.previewDimension")));
        buttonList.add(new GuiButton(4, width / 2 + 5, top + 76, 150, 20, I18n.format("ttgtnh.gui.previewBiome")));
        buttonList.add(new GuiButton(5, width / 2 - 100, height - 28, I18n.format("gui.done")));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id < 3) {
            String category = new String[] { "general", "dimension", "biome" }[button.id];
            mc.displayGuiScreen(
                new GuiConfig(
                    this,
                    new ConfigElement<>(ClientConfig.config.getCategory(category)).getChildElements(),
                    TravelersTitles.ID,
                    false,
                    false,
                    "Traveler's Titles GTNH",
                    I18n.format("ttgtnh.gui." + category)));
        } else if (button.id < 5) {
            mc.displayGuiScreen(new TitlePreviewScreen(this, button.id == 3 ? "dimension" : "biome"));
        } else mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (key == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(character, key);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Traveler's Titles GTNH", width / 2, 15, 0xffffff);
        int y = 145;
        for (String line : fontRendererObj
            .listFormattedStringToWidth(I18n.format("ttgtnh.gui.help"), Math.min(360, width - 20))) {
            drawCenteredString(fontRendererObj, line, width / 2, y, 0xaaaaaa);
            y += 10;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
