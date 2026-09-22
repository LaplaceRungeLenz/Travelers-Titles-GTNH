package io.github.laplacerungelenz.travelerstitles.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.resources.I18n;

/** Identified by type so another mod's button IDs cannot open our screen. */
final class PauseConfigButton extends GuiButton {

    PauseConfigButton(int x, int y) {
        super(0x5454, x, y, 150, 20, I18n.format("ttgtnh.gui.pause"));
    }
}
