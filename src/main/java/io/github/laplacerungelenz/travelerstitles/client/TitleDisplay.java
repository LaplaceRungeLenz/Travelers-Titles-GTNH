package io.github.laplacerungelenz.travelerstitles.client;

import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

/** Prepared outside the render callback; texture existence checks never run per frame. */
public final class TitleDisplay {

    public final TitleStyle style;
    public final String title, subtitle;
    public final long start;
    public final boolean image, backgroundImage, icon;
    public final int subtitleColor;

    public TitleDisplay(TitleStyle style, String title, String subtitle, long start, TitleResources resources) {
        this(style, title, subtitle, start, resources, style.subtitleColor);
    }

    public TitleDisplay(TitleStyle style, String title, String subtitle, long start, TitleResources resources,
        int subtitleColor) {
        this.style = style;
        this.title = title;
        this.subtitle = subtitle;
        this.start = start;
        this.subtitleColor = subtitleColor;
        image = resources.textureAvailable(style.texture);
        backgroundImage = resources.textureAvailable(style.backgroundTexture);
        icon = resources.textureAvailable(style.icon);
    }
}
