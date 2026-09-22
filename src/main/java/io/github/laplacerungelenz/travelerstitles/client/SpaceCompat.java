package io.github.laplacerungelenz.travelerstitles.client;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import io.github.laplacerungelenz.travelerstitles.TravelersTitles;

/** Only public, client-safe optional API calls; cached lookup avoids hard linkage to GC addons. */
public final class SpaceCompat {

    private static final Map<String, Method> METHODS = new HashMap<>();
    private static final Set<String> MISSING = new HashSet<>();
    private static final Set<String> WARNED = new HashSet<>();

    private SpaceCompat() {}

    public static Object call(Object target, String name) {
        if (target == null) return null;
        String key = target.getClass()
            .getName() + "#"
            + name;
        if (MISSING.contains(key)) return null;
        try {
            Method method = METHODS.get(key);
            if (method == null) {
                method = target.getClass()
                    .getMethod(name);
                METHODS.put(key, method);
            }
            return method.invoke(target);
        } catch (NoSuchMethodException ex) {
            MISSING.add(key);
        } catch (ReflectiveOperationException | LinkageError | RuntimeException ex) {
            if (WARNED.add(key))
                TravelersTitles.LOG.warn("Optional title adapter unavailable: {} ({})", key, ex.toString());
        }
        return null;
    }

    public static String string(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static Map<String, String> describe(Object provider) {
        Map<String, String> values = new LinkedHashMap<>();
        Object body = call(provider, "getCelestialBody");
        if (body == null) return values;
        values.put("body", string(call(body, "getUnlocalizedName")));
        values.put("bodyName", string(call(body, "getName")));
        values.put("bodyTitle", string(call(body, "getLocalizedName")));
        String className = provider.getClass()
            .getName();
        boolean ship = className.contains("Mothership");
        Object orbit = call(provider, "getPlanetToOrbit");
        values.put("spaceKind", ship ? "mothership" : orbit != null ? "station" : "body");
        Object parent = call(body, "getParentPlanet");
        if (parent == null) parent = call(body, "getParentBody");
        if (ship) parent = call(provider, "getParent");
        String orbitName = string(call(parent, "getUnlocalizedName"));
        values.put("orbit", orbitName.isEmpty() ? string(orbit) : orbitName);
        values.put("orbitTitle", string(call(parent, "getLocalizedName")));
        values.put("transit", string(call(provider, "isInTransit")));
        return values;
    }

    public static Map<String, Object> registry() {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            Class<?> type = Class.forName("micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry");
            for (String method : new String[] { "getRegisteredPlanets", "getRegisteredMoons",
                "getRegisteredSatellites" }) {
                Object map = type.getMethod(method)
                    .invoke(null);
                if (map instanceof Map) for (Object body : ((Map<?, ?>) map).values()) {
                    result.put(string(call(body, "getUnlocalizedName")), body);
                }
            }
        } catch (ReflectiveOperationException | LinkageError ex) { /* Optional mod absent. */ }
        return result;
    }
}
