package io.github.laplacerungelenz.travelerstitles.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import io.github.laplacerungelenz.travelerstitles.core.Animation;
import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

public final class HudRenderer extends Gui {

    public void render(TitleDisplay d, long tick, float partialTicks) {
        if (d == null) return;
        TitleStyle s = d.style;
        float alpha = Animation.alpha(tick - d.start + partialTicks, s.fadeIn, s.hold, s.fadeOut);
        if (alpha <= 0.035F) return;
        render(d, alpha);
    }

    public void renderPreview(TitleDisplay display) {
        if (display != null) render(display, 1);
    }

    private void render(TitleDisplay d, float alpha) {
        TitleStyle s = d.style;
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution screen = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        float maxWidth = screen.getScaledWidth() * s.maxWidth;
        float textScale = Math.min(s.scale, maxWidth / Math.max(1, mc.fontRenderer.getStringWidth(d.title)));
        float width = d.image ? Math.min(s.imageWidth, maxWidth) : mc.fontRenderer.getStringWidth(d.title) * textScale;
        float height = d.image ? width * s.imageHeight / s.imageWidth : mc.fontRenderer.FONT_HEIGHT * textScale;
        float subScale = Math.min(s.subtitleScale, maxWidth / Math.max(1, mc.fontRenderer.getStringWidth(d.subtitle)));
        float fullWidth = Math.max(width, mc.fontRenderer.getStringWidth(d.subtitle) * subScale);
        float fullHeight = height + (d.subtitle.isEmpty() ? 0 : s.lineSpacing + mc.fontRenderer.FONT_HEIGHT * subScale);
        GL11.glPushAttrib(
            GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT
                | GL11.GL_CURRENT_BIT
                | GL11.GL_DEPTH_BUFFER_BIT
                | GL11.GL_TEXTURE_BIT);
        GL11.glPushMatrix();
        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glTranslatef(screen.getScaledWidth() * s.anchorX + s.x, screen.getScaledHeight() * s.anchorY + s.y, 0);
            int opacity = Math.round(alpha * 255) << 24;
            if (s.background) drawRect(
                (int) (-fullWidth / 2 - 10),
                -6,
                (int) (fullWidth / 2 + 10),
                (int) (fullHeight + 6),
                Math.round(alpha * 140) << 24);
            // Gui.drawRect disables blending on return.
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            if (d.backgroundImage)
                texture(s.backgroundTexture, -fullWidth / 2 - 12, -8, fullWidth + 24, fullHeight + 16, alpha);
            if (d.icon) texture(s.icon, -width / 2 - height - 6, 0, height, height, alpha);
            if (d.image) texture(s.texture, -width / 2, 0, width, height, alpha);
            else text(mc, d.title, 0, textScale, s.color | opacity, s.shadow);
            if (!d.subtitle.isEmpty())
                text(mc, d.subtitle, height + s.lineSpacing, subScale, s.subtitleColor | opacity, s.shadow);
            if (s.decoration) drawRect(
                (int) (-Math.max(36, width * 0.6F) / 2),
                (int) (fullHeight + 5),
                (int) (Math.max(36, width * 0.6F) / 2),
                (int) (fullHeight + 6),
                s.color | Math.round(alpha * 150) << 24);
        } finally {
            GL11.glPopMatrix();
            GL11.glPopAttrib();
        }
    }

    private static void text(Minecraft mc, String text, float y, float scale, int color, boolean shadow) {
        GL11.glPushMatrix();
        try {
            GL11.glTranslatef(0, y, 0);
            GL11.glScalef(scale, scale, 1);
            mc.fontRenderer.drawString(text, -mc.fontRenderer.getStringWidth(text) / 2, 0, color, shadow);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private static void texture(String name, float x, float y, float width, float height, float alpha) {
        Minecraft.getMinecraft()
            .getTextureManager()
            .bindTexture(new ResourceLocation(name));
        GL11.glColor4f(1, 1, 1, alpha);
        Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        t.addVertexWithUV(x, y + height, 0, 0, 1);
        t.addVertexWithUV(x + width, y + height, 0, 1, 1);
        t.addVertexWithUV(x + width, y, 0, 1, 0);
        t.addVertexWithUV(x, y, 0, 0, 0);
        t.draw();
    }
}
