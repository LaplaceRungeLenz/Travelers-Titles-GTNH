package io.github.laplacerungelenz.travelerstitles.client;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;

/** TextureManager reloads registered textures before title rules: validate on every load. */
public final class SafeTitleTexture extends AbstractTexture {

    private final ResourceLocation resource;

    public SafeTitleTexture(ResourceLocation resource) {
        this.resource = resource;
    }

    @Override
    public void loadTexture(IResourceManager manager) throws IOException {
        BufferedImage image = null;
        try {
            IResource entry = manager.getResource(resource);
            try (InputStream stream = entry.getInputStream()) {
                image = ImageIO.read(stream);
            }
            if (image == null) throw new IOException("Invalid title image: " + resource);
            TextureMetadataSection metadata = entry.hasMetadata()
                ? (TextureMetadataSection) entry.getMetadata("texture")
                : null;
            deleteGlTexture();
            TextureUtil.uploadTextureImageAllocate(
                getGlTextureId(),
                image,
                metadata != null && metadata.getTextureBlur(),
                metadata != null && metadata.getTextureClamp());
        } catch (RuntimeException ex) {
            throw new IOException("Cannot load title image: " + resource, ex);
        } finally {
            if (image != null) image.flush();
        }
    }
}
