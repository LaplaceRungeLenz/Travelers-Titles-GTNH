package io.github.laplacerungelenz.travelerstitles.client;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.IMetadataSection;
import net.minecraft.util.ResourceLocation;

import org.junit.Test;

public class SafeTitleTextureTest {

    @Test
    public void invalidImageIsIoFailureOnEveryReloadBeforeAnyGlCall() throws Exception {
        SafeTitleTexture texture = new SafeTitleTexture(new ResourceLocation("test:image.png"));
        for (int reload = 0; reload < 2; reload++) {
            try {
                texture.loadTexture(manager("not a PNG".getBytes(StandardCharsets.UTF_8)));
                fail("Invalid image must fail with IOException rather than renderer NPE");
            } catch (IOException expected) {
                assertTrue(
                    expected.getMessage()
                        .contains("image"));
            }
        }
    }

    @Test
    public void truncatedPngIsIoFailureBeforeAnyGlCall() throws Exception {
        try {
            new SafeTitleTexture(new ResourceLocation("test:broken.png"))
                .loadTexture(manager(new byte[] { (byte) 137, 80, 78, 71, 13, 10, 26, 10 }));
            fail("Truncated PNG must fail");
        } catch (IOException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    private static IResourceManager manager(final byte[] data) {
        final IResource resource = new IResource() {

            public InputStream getInputStream() {
                return new ByteArrayInputStream(data);
            }

            public boolean hasMetadata() {
                return false;
            }

            public IMetadataSection getMetadata(String section) {
                return null;
            }
        };
        return new IResourceManager() {

            public Set<String> getResourceDomains() {
                return Collections.singleton("test");
            }

            public IResource getResource(ResourceLocation path) {
                return resource;
            }

            public List<IResource> getAllResources(ResourceLocation path) {
                return Collections.singletonList(resource);
            }
        };
    }
}
