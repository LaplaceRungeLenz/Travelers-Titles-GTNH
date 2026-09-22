package io.github.laplacerungelenz.travelerstitles.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Runtime identity is separate from translated presentation. */
public final class Location {

    public final String dimensionKey;
    public final String biomeKey;
    public final Map<String, String> attributes;

    public Location(String dimensionKey, String biomeKey, Map<String, String> attributes) {
        this.dimensionKey = dimensionKey;
        this.biomeKey = dimensionKey + "|" + biomeKey;
        this.attributes = Collections.unmodifiableMap(new LinkedHashMap<>(attributes));
    }

    public String get(String key) {
        return attributes.containsKey(key) ? attributes.get(key) : "";
    }
}
