package io.github.laplacerungelenz.travelerstitles.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Deterministic field inheritance. Local overrides always outrank resource packs. */
public final class RuleEngine {

    public static final int LOCAL = 1000000;
    private static final Set<String> MATCHERS = new HashSet<>(
        Arrays.asList(
            "kind",
            "dimension",
            "dimensionId",
            "provider",
            "body",
            "bodyName",
            "orbit",
            "biome",
            "biomeId",
            "biomeClass",
            "biomeName",
            "types",
            "spaceKind"));
    private final Map<String, Rule> rules = new LinkedHashMap<>();
    private final List<DefaultLayer> defaults = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public void load(String json, String source, int layer) {
        try {
            JsonObject root = new JsonParser().parse(json)
                .getAsJsonObject();
            if (!root.has("schemaVersion") || root.get("schemaVersion")
                .getAsInt() != 1) {
                warn(source + ": unsupported or missing schemaVersion");
                return;
            }
            if (root.has("defaults")) {
                JsonObject all = root.getAsJsonObject("defaults");
                for (String kind : new String[] { "dimension", "biome" }) {
                    if (all.has(kind))
                        defaults.add(new DefaultLayer(kind, clean(all.getAsJsonObject(kind), source), layer, source));
                }
            }
            if (!root.has("rules")) return;
            for (JsonElement e : root.getAsJsonArray("rules")) {
                try {
                    loadRule(e.getAsJsonObject(), source, layer);
                } catch (RuntimeException ex) {
                    warn(source + ": invalid rule: " + ex.getMessage());
                }
            }
        } catch (RuntimeException ex) {
            warn(source + ": invalid document: " + ex.getMessage());
        }
    }

    private void loadRule(JsonObject j, String source, int layer) {
        String id = j.get("id")
            .getAsString();
        if (id.isEmpty() || id.length() > 200) throw new IllegalArgumentException("invalid id");
        Rule previous = rules.get(id);
        JsonObject match = j.has("match") ? j.getAsJsonObject("match")
            : previous == null ? new JsonObject() : previous.match;
        for (Map.Entry<String, JsonElement> entry : match.entrySet()) {
            if (!MATCHERS.contains(entry.getKey()))
                throw new IllegalArgumentException("unknown matcher " + entry.getKey());
            if ("types".equals(entry.getKey())) {
                for (JsonElement type : entry.getValue()
                    .getAsJsonArray()) type.getAsString();
            } else if (!entry.getValue()
                .isJsonPrimitive()) throw new IllegalArgumentException("matcher must be scalar");
        }
        JsonObject style = new JsonObject();
        if (previous != null) merge(style, previous.style);
        if (j.has("style")) merge(style, clean(j.getAsJsonObject("style"), source + "/" + id));
        int priority = j.has("priority") ? j.get("priority")
            .getAsInt() : previous == null ? 0 : previous.priority;
        rules.put(id, new Rule(id, priority, layer, match, style, source));
    }

    public TitleStyle resolve(String kind, Location location) {
        return resolve(kind, location, new JsonObject());
    }

    public TitleStyle resolve(String kind, Location location, JsonObject appearance) {
        JsonObject result = TitleStyle.defaults(kind);
        String source = "built-in";
        for (DefaultLayer d : defaults) if (d.layer < LOCAL && d.kind.equals(kind)) {
            merge(result, d.style);
            source = d.source;
        }
        List<Rule> matches = new ArrayList<>();
        for (Rule r : rules.values()) if (r.matches(kind, location)) matches.add(r);
        Collections.sort(
            matches,
            Comparator.comparingInt((Rule r) -> r.layer >= LOCAL ? 1 : 0)
                .thenComparingInt(r -> r.priority)
                .thenComparingInt(
                    r -> r.match.entrySet()
                        .size())
                .thenComparingInt(r -> r.layer)
                .thenComparing(r -> r.id));
        for (Rule r : matches) if (r.layer < LOCAL) {
            merge(result, r.style);
            source = r.source + "/" + r.id;
        }
        for (DefaultLayer d : defaults) if (d.layer >= LOCAL && d.kind.equals(kind)) {
            merge(result, d.style);
            source = d.source;
        }
        for (Rule r : matches) if (r.layer >= LOCAL) {
            merge(result, r.style);
            source = r.source + "/" + r.id;
        }
        if (!appearance.entrySet()
            .isEmpty()) {
            merge(result, clean(appearance, "in-game appearance"));
            source += " + in-game appearance";
        }
        return new TitleStyle(result, source);
    }

    public List<String> warnings() {
        return Collections.unmodifiableList(warnings);
    }

    private void warn(String text) {
        if (warnings.size() < 100) warnings.add(text);
    }

    private static void merge(JsonObject into, JsonObject from) {
        // Explicit subtitle colors (including GUI appearance overrides) opt out of automatic biome colors.
        if (from.has("subtitleColor") && !from.has("biomeSubtitleColor")) into.addProperty("biomeSubtitleColor", false);
        // An unrelated replacement image must not inherit the old atlas crop.
        if (from.has("texture") && into.has("texture")
            && !from.get("texture")
                .equals(into.get("texture"))) {
            into.addProperty("textureU0", 0);
            into.addProperty("textureV0", 0);
            into.addProperty("textureU1", 1);
            into.addProperty("textureV1", 1);
        }
        for (Map.Entry<String, JsonElement> e : from.entrySet()) into.add(e.getKey(), e.getValue());
    }

    private JsonObject clean(JsonObject values, String source) {
        JsonObject result = new JsonObject(), template = TitleStyle.defaults("dimension");
        for (Map.Entry<String, JsonElement> e : values.entrySet()) {
            String key = e.getKey();
            try {
                if (!template.has(key) || !e.getValue()
                    .isJsonPrimitive()) throw new IllegalArgumentException("unknown field/type");
                if (template.get(key)
                    .getAsJsonPrimitive()
                    .isBoolean()) {
                    if (!e.getValue()
                        .getAsJsonPrimitive()
                        .isBoolean()) throw new IllegalArgumentException("expected boolean");
                    result.addProperty(
                        key,
                        e.getValue()
                            .getAsBoolean());
                } else if (template.get(key)
                    .getAsJsonPrimitive()
                    .isNumber()) {
                        double v = e.getValue()
                            .getAsDouble();
                        double min = 0, max = 12000;
                        if (key.equals("scale") || key.equals("subtitleScale")) {
                            min = 0.1;
                            max = 10;
                        } else if (key.equals("anchorX") || key.equals("anchorY")
                            || key.equals("volume")
                            || key.startsWith("textureU")
                            || key.startsWith("textureV")) max = 1;
                        else if (key.equals("maxWidth")) {
                            min = 0.05;
                            max = 1;
                        } else if (key.equals("x") || key.equals("y")) {
                            min = -4096;
                            max = 4096;
                        } else if (key.equals("imageWidth") || key.equals("imageHeight")) {
                            min = 1;
                            max = 4096;
                        } else if (key.equals("pitch")) {
                            min = 0.5;
                            max = 2;
                        } else if (key.equals("lineSpacing")) max = 100;
                        if (Double.isNaN(v) || Double.isInfinite(v) || v < min || v > max)
                            throw new IllegalArgumentException("out of range");
                        result.addProperty(key, v);
                    } else {
                        String v = e.getValue()
                            .getAsString();
                        if (v.length() > 2048) throw new IllegalArgumentException("too long");
                        if (key.equals("color") || key.equals("subtitleColor")) {
                            v = v.replaceFirst("^#", "");
                            if (!v.matches("[0-9a-fA-F]{6}")) throw new IllegalArgumentException("expected RRGGBB");
                        }
                        if ((key.equals("texture") || key.equals("backgroundTexture")
                            || key.equals("icon")
                            || key.equals("sound")) && !v.isEmpty()
                            && (!v.matches("[a-z0-9_.-]+:[a-zA-Z0-9_./-]+") || v.contains("..")))
                            throw new IllegalArgumentException("invalid resource location");
                        result.addProperty(key, v);
                    }
            } catch (RuntimeException ex) {
                warn(source + ": ignored " + key + " (" + ex.getMessage() + ")");
            }
        }
        return result;
    }

    private static final class DefaultLayer {

        final String kind, source;
        final JsonObject style;
        final int layer;

        DefaultLayer(String kind, JsonObject style, int layer, String source) {
            this.kind = kind;
            this.style = style;
            this.layer = layer;
            this.source = source;
        }
    }

    private static final class Rule {

        final String id, source;
        final int priority, layer;
        final JsonObject match, style;
        final Map<String, Pattern> patterns = new LinkedHashMap<>();

        Rule(String id, int priority, int layer, JsonObject match, JsonObject style, String source) {
            this.id = id;
            this.priority = priority;
            this.layer = layer;
            this.match = match;
            this.style = style;
            this.source = source;
            for (Map.Entry<String, JsonElement> e : match.entrySet()) {
                if (e.getKey()
                    .equals("types")) continue;
                StringBuilder regex = new StringBuilder("^");
                for (char ch : e.getValue()
                    .getAsString()
                    .toCharArray()) regex.append(ch == '*' ? ".*" : Pattern.quote(String.valueOf(ch)));
                patterns.put(
                    e.getKey(),
                    Pattern.compile(
                        regex.append('$')
                            .toString()));
            }
        }

        boolean matches(String kind, Location location) {
            for (Map.Entry<String, Pattern> e : patterns.entrySet()) {
                String value = e.getKey()
                    .equals("kind") ? kind : location.get(e.getKey());
                if (!e.getValue()
                    .matcher(value)
                    .matches()) return false;
            }
            if (match.has("types")) {
                List<String> types = Arrays.asList(
                    location.get("types")
                        .split(","));
                for (JsonElement type : match.getAsJsonArray("types"))
                    if (!types.contains(type.getAsString())) return false;
            }
            return true;
        }
    }
}
