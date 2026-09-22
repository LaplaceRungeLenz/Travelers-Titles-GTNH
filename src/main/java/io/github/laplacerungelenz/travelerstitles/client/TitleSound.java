package io.github.laplacerungelenz.travelerstitles.client;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.util.ResourceLocation;

/** Non-positional UI sound, unaffected by a planet's distance attenuation. */
public final class TitleSound extends PositionedSound {

    public TitleSound(String name, float volume, float pitch) {
        super(new ResourceLocation(name));
        this.volume = volume;
        this.field_147663_c = pitch;
        this.field_147666_i = ISound.AttenuationType.NONE;
    }
}
