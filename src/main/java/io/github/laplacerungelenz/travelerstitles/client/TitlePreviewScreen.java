package io.github.laplacerungelenz.travelerstitles.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

import io.github.laplacerungelenz.travelerstitles.core.Location;
import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

/** A still preview uses the real HUD renderer, without triggering sounds or the title controller. */
public final class TitlePreviewScreen extends GuiScreen {

    private final GuiScreen parent;
    private final String kind;
    private final HudRenderer renderer = new HudRenderer();
    private TitleDisplay display;

    public TitlePreviewScreen(GuiScreen parent, String kind) {
        this.parent = parent;
        this.kind = kind;
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height - 28, I18n.format("gui.done")));
        TitleResources resources = ClientProxy.instance.resources;
        Location location = new LocationResolver().sample(mc);
        TitleStyle style = location == null ? resources.engine()
            .resolve(kind, new Location("", "", java.util.Collections.emptyMap()), ClientConfig.appearance(kind))
            : resources.resolve(kind, location);
        String title = location == null ? I18n.format("ttgtnh.gui.sampleTitle")
            : TitleResources.name(kind, location, style);
        String subtitle = location == null ? I18n.format("ttgtnh.gui.sampleSubtitle")
            : TitleResources.subtitle(kind, location, style, resources.engine());
        if (!style.showSubtitle || title.equals(subtitle)) subtitle = "";
        display = new TitleDisplay(style, title, subtitle, 0, resources);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        mc.displayGuiScreen(parent);
    }

    @Override
    protected void keyTyped(char character, int key) {
        if (key == 1) mc.displayGuiScreen(parent);
        else super.keyTyped(character, key);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        renderer.renderPreview(display);
        drawCenteredString(fontRendererObj, I18n.format("ttgtnh.gui.previewHelp"), width / 2, height - 42, 0xffffff);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
