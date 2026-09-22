package io.github.laplacerungelenz.travelerstitles.core;

import com.google.gson.JsonObject;

/** Validated presentation snapshot. Never contains Minecraft or GPU objects. */
public final class TitleStyle {

    public final boolean enabled, shadow, showSubtitle, background, decoration;
    public final String title, titleKey, subtitle, subtitleKey, alias, texture, backgroundTexture, icon, sound;
    public final int color, subtitleColor, fadeIn, hold, fadeOut, imageWidth, imageHeight;
    public final float scale, subtitleScale, anchorX, anchorY, x, y, maxWidth, lineSpacing, volume, pitch;
    public final String source;

    public TitleStyle(JsonObject j, String source) {
        enabled = j.get("enabled")
            .getAsBoolean();
        shadow = j.get("shadow")
            .getAsBoolean();
        showSubtitle = j.get("showSubtitle")
            .getAsBoolean();
        background = j.get("background")
            .getAsBoolean();
        decoration = j.get("decoration")
            .getAsBoolean();
        title = string(j, "title");
        titleKey = string(j, "titleKey");
        subtitle = string(j, "subtitle");
        subtitleKey = string(j, "subtitleKey");
        alias = string(j, "alias");
        texture = string(j, "texture");
        backgroundTexture = string(j, "backgroundTexture");
        icon = string(j, "icon");
        sound = string(j, "sound");
        color = Integer.parseInt(string(j, "color"), 16);
        subtitleColor = Integer.parseInt(string(j, "subtitleColor"), 16);
        fadeIn = integer(j, "fadeIn");
        hold = integer(j, "hold");
        fadeOut = integer(j, "fadeOut");
        imageWidth = integer(j, "imageWidth");
        imageHeight = integer(j, "imageHeight");
        scale = number(j, "scale");
        subtitleScale = number(j, "subtitleScale");
        anchorX = number(j, "anchorX");
        anchorY = number(j, "anchorY");
        x = number(j, "x");
        y = number(j, "y");
        maxWidth = number(j, "maxWidth");
        lineSpacing = number(j, "lineSpacing");
        volume = number(j, "volume");
        pitch = number(j, "pitch");
        this.source = source;
    }

    public int duration() {
        return fadeIn + hold + fadeOut;
    }

    private static String string(JsonObject j, String key) {
        return j.get(key)
            .getAsString();
    }

    private static int integer(JsonObject j, String key) {
        return j.get(key)
            .getAsInt();
    }

    private static float number(JsonObject j, String key) {
        return j.get(key)
            .getAsFloat();
    }

    public static JsonObject defaults(String kind) {
        JsonObject j = new JsonObject();
        for (String key : new String[] { "title", "titleKey", "subtitle", "subtitleKey", "alias", "texture",
            "backgroundTexture", "icon", "sound" }) j.addProperty(key, "");
        j.addProperty("enabled", true);
        j.addProperty("shadow", true);
        j.addProperty("showSubtitle", true);
        j.addProperty("background", false);
        j.addProperty("decoration", true);
        j.addProperty("color", "ffffff");
        j.addProperty("subtitleColor", "cccccc");
        j.addProperty("fadeIn", 10);
        j.addProperty("hold", "dimension".equals(kind) ? 70 : 50);
        j.addProperty("fadeOut", 20);
        j.addProperty("scale", "dimension".equals(kind) ? 3.0 : 2.1);
        j.addProperty("subtitleScale", 1.1);
        j.addProperty("anchorX", 0.5);
        j.addProperty("anchorY", 0.23);
        j.addProperty("x", 0);
        j.addProperty("y", 0);
        j.addProperty("maxWidth", 0.85);
        j.addProperty("lineSpacing", 7);
        j.addProperty("volume", 0.35);
        j.addProperty("pitch", 1.0);
        j.addProperty("imageWidth", 256);
        j.addProperty("imageHeight", 64);
        return j;
    }
}
