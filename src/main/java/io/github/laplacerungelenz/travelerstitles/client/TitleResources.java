package io.github.laplacerungelenz.travelerstitles.client;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.github.laplacerungelenz.travelerstitles.TravelersTitles;
import io.github.laplacerungelenz.travelerstitles.core.Location;
import io.github.laplacerungelenz.travelerstitles.core.RuleEngine;
import io.github.laplacerungelenz.travelerstitles.core.TitleStyle;

public final class TitleResources implements IResourceManagerReloadListener {

    private RuleEngine engine = new RuleEngine();
    private final Map<String, Boolean> textures = new HashMap<>();
    private int generation;

    @Override
    public void onResourceManagerReload(IResourceManager manager) {
        RuleEngine next = new RuleEngine();
        try {
            List<IResource> indices = manager
                .getAllResources(new ResourceLocation(TravelersTitles.ID, "titles/index.json"));
            int layer = 0;
            for (IResource index : indices) {
                String source = "index[" + layer + "]";
                try {
                    String data = read(index.getInputStream());
                    JsonObject json = new JsonParser().parse(data)
                        .getAsJsonObject();
                    if (json.has("files")) for (JsonElement entry : json.getAsJsonArray("files")) {
                        String path = entry.getAsString();
                        if (!path.matches("[a-z0-9_./-]+\\.json") || path.contains("..")) continue;
                        try {
                            IResource resource = manager
                                .getResource(new ResourceLocation(TravelersTitles.ID, "titles/" + path));
                            next.load(read(resource.getInputStream()), source + "/" + path, layer);
                        } catch (IOException | RuntimeException ex) {
                            TravelersTitles.LOG.warn("Cannot read title include {}: {}", path, ex.toString());
                        }
                    }
                    next.load(data, source, layer);
                } catch (IOException | RuntimeException ex) {
                    TravelersTitles.LOG.warn("Cannot read title index {}: {}", source, ex.toString());
                }
                layer++;
            }
        } catch (IOException ex) {
            TravelersTitles.LOG.warn("No title index found; using fallback styles");
        }
        File overrides = new File(ClientConfig.directory, "overrides.json");
        if (overrides.isFile()) {
            try {
                next.load(read(Files.newInputStream(overrides.toPath())), "local overrides", RuleEngine.LOCAL);
            } catch (IOException ex) {
                TravelersTitles.LOG.warn("Cannot read title overrides: {}", ex.toString());
            }
        }
        engine = next;
        textures.clear();
        generation++;
        for (String warning : next.warnings()) TravelersTitles.LOG.warn(warning);
        TravelersTitles.LOG.info(
            "Title resources reloaded; {} warnings",
            next.warnings()
                .size());
    }

    public int generation() {
        return generation;
    }

    public RuleEngine engine() {
        return engine;
    }

    public TitleStyle resolve(String kind, Location location) {
        return engine.resolve(kind, location, ClientConfig.appearance(kind));
    }

    public boolean textureAvailable(String name) {
        if (name.isEmpty()) return false;
        Boolean value = textures.get(name);
        if (value == null) {
            try (InputStream stream = Minecraft.getMinecraft()
                .getResourceManager()
                .getResource(new ResourceLocation(name))
                .getInputStream()) {
                BufferedImage decoded = ImageIO.read(stream);
                value = decoded != null && decoded.getWidth() > 0 && decoded.getHeight() > 0;
                if (decoded != null) decoded.flush();
                if (value) {
                    ResourceLocation resource = new ResourceLocation(name);
                    value = Minecraft.getMinecraft()
                        .getTextureManager()
                        .loadTexture(resource, new SafeTitleTexture(resource));
                }
                if (!value) TravelersTitles.LOG.warn("Invalid title image: {} (using text fallback)", name);
            } catch (IOException | RuntimeException ex) {
                value = false;
                TravelersTitles.LOG
                    .warn("Title texture unavailable: {} (using text fallback): {}", name, ex.toString());
            }
            textures.put(name, value);
        }
        return value;
    }

    public static String name(String kind, Location location, TitleStyle style) {
        if (!style.title.isEmpty()) return style.title;
        if (!style.titleKey.isEmpty() && StatCollector.canTranslate(style.titleKey))
            return StatCollector.translateToLocal(style.titleKey);
        String alias = !style.alias.isEmpty() ? style.alias : location.get(kind);
        String key = TravelersTitles.ID + "." + kind + "." + alias.replace(':', '.');
        if (StatCollector.canTranslate(key)) return StatCollector.translateToLocal(key);
        if (kind.equals("dimension")) {
            String body = location.get("bodyTitle");
            if (!body.isEmpty() && !body.equals(location.get("body"))) return body;
            if (!location.get("dimensionName")
                .isEmpty()) return location.get("dimensionName");
            return "Dimension " + location.get("dimensionId");
        }
        for (String candidate : new String[] { "biome." + alias.replace(':', '.'), "biome." + location.get("biomeName"),
            "biome." + LocationResolver.slug(location.get("biomeName")) }) {
            if (StatCollector.canTranslate(candidate)) return StatCollector.translateToLocal(candidate);
        }
        return location.get("biomeName")
            .isEmpty() ? "Biome " + location.get("biomeId") : location.get("biomeName");
    }

    public static String subtitle(String kind, Location location, TitleStyle style, RuleEngine engine) {
        if (!style.showSubtitle) return "";
        if (!style.subtitle.isEmpty()) return style.subtitle;
        if (!style.subtitleKey.isEmpty() && StatCollector.canTranslate(style.subtitleKey))
            return StatCollector.translateToLocal(style.subtitleKey);
        if ("mothership".equals(location.get("spaceKind"))) {
            if ("true".equals(location.get("transit"))) return StatCollector.translateToLocal("ttgtnh.inTransit");
            if (!location.get("orbitTitle")
                .isEmpty()) return location.get("orbitTitle");
        }
        if (kind.equals("dimension") && ClientConfig.biomes) {
            TitleStyle biome = engine.resolve("biome", location);
            if (biome.enabled) return name("biome", location, biome);
        }
        return "";
    }

    public static int subtitleColor(String kind, Location location, TitleStyle style, TitleStyle biome) {
        if (!style.biomeSubtitleColor || !"dimension".equals(kind)
            || !biome.enabled
            || !style.subtitle.isEmpty()
            || !style.subtitleKey.isEmpty()
            || "mothership".equals(location.get("spaceKind"))) return style.subtitleColor;
        return biome.color;
    }

    private static String read(InputStream stream) throws IOException {
        try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            StringBuilder data = new StringBuilder();
            char[] buffer = new char[4096];
            int count;
            while ((count = reader.read(buffer)) != -1) {
                if (data.length() + count > 1048576) throw new IOException("Title JSON exceeds 1 MiB");
                data.append(buffer, 0, count);
            }
            return data.toString();
        }
    }
}
