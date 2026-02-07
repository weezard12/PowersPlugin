package me.weezard12.powers.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Cross-version helpers for PotionEffectType resolution and PotionEffect creation.
 */
public final class PotionEffects {
    private static final Map<String, String> ALIASES;
    private static final Constructor<PotionEffect> CTOR_6;
    private static final Constructor<PotionEffect> CTOR_5;
    private static final Constructor<PotionEffect> CTOR_4;
    private static final Constructor<PotionEffect> CTOR_3;

    static {
        Map<String, String> aliases = new HashMap<String, String>();
        aliases.put("jump_boost", "JUMP");
        aliases.put("jump", "JUMP");
        aliases.put("haste", "FAST_DIGGING");
        aliases.put("mining_fatigue", "SLOW_DIGGING");
        aliases.put("strength", "INCREASE_DAMAGE");
        aliases.put("instant_health", "HEAL");
        aliases.put("instant_damage", "HARM");
        aliases.put("nausea", "CONFUSION");
        aliases.put("resistance", "DAMAGE_RESISTANCE");
        aliases.put("slowness", "SLOW");
        aliases.put("speed", "SPEED");
        aliases.put("regen", "REGENERATION");
        aliases.put("fire_resistance", "FIRE_RESISTANCE");
        aliases.put("water_breathing", "WATER_BREATHING");
        aliases.put("night_vision", "NIGHT_VISION");
        aliases.put("blindness", "BLINDNESS");
        aliases.put("invisibility", "INVISIBILITY");
        aliases.put("weakness", "WEAKNESS");
        aliases.put("poison", "POISON");
        aliases.put("wither", "WITHER");
        aliases.put("absorption", "ABSORPTION");
        aliases.put("health_boost", "HEALTH_BOOST");
        aliases.put("saturation", "SATURATION");
        aliases.put("glowing", "GLOWING");
        aliases.put("levitation", "LEVITATION");
        aliases.put("luck", "LUCK");
        aliases.put("unluck", "UNLUCK");
        aliases.put("slow_falling", "SLOW_FALLING");
        aliases.put("conduit_power", "CONDUIT_POWER");
        aliases.put("dolphins_grace", "DOLPHINS_GRACE");
        aliases.put("bad_omen", "BAD_OMEN");
        aliases.put("hero_of_the_village", "HERO_OF_THE_VILLAGE");
        aliases.put("darkness", "DARKNESS");
        ALIASES = Collections.unmodifiableMap(aliases);

        CTOR_6 = findConstructor(PotionEffectType.class, int.class, int.class, boolean.class, boolean.class, boolean.class);
        CTOR_5 = findConstructor(PotionEffectType.class, int.class, int.class, boolean.class, boolean.class);
        CTOR_4 = findConstructor(PotionEffectType.class, int.class, int.class, boolean.class);
        CTOR_3 = findConstructor(PotionEffectType.class, int.class, int.class);
    }

    private PotionEffects() {
    }

    /**
     * Resolve a PotionEffectType from a user-friendly string across versions.
     * Supports aliases and namespaced keys (e.g., "minecraft:speed").
     */
    public static PotionEffectType resolve(String input) {
        if (input == null) {
            return null;
        }
        String raw = input.trim();
        if (raw.isEmpty()) {
            return null;
        }

        String normalized = normalize(raw);
        PotionEffectType type = PotionEffectType.getByName(normalized);
        if (type != null) {
            return type;
        }

        String alias = ALIASES.get(normalized.toLowerCase(Locale.ROOT));
        if (alias != null) {
            type = PotionEffectType.getByName(alias);
            if (type != null) {
                return type;
            }
        }

        PotionEffectType byKey = getByKey(normalized.toLowerCase(Locale.ROOT));
        if (byKey != null) {
            return byKey;
        }

        PotionEffectType byId = getById(normalized);
        if (byId != null) {
            return byId;
        }

        return null;
    }

    /**
     * Create a PotionEffect using the most detailed constructor available on this server.
     */
    public static PotionEffect createEffect(PotionEffectType type, int durationTicks, int amplifier,
                                            boolean ambient, boolean particles, boolean icon) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        int safeDuration = Math.max(1, durationTicks);
        int safeAmplifier = Math.max(0, amplifier);
        try {
            if (CTOR_6 != null) {
                return CTOR_6.newInstance(type, safeDuration, safeAmplifier, ambient, particles, icon);
            }
            if (CTOR_5 != null) {
                return CTOR_5.newInstance(type, safeDuration, safeAmplifier, ambient, particles);
            }
            if (CTOR_4 != null) {
                return CTOR_4.newInstance(type, safeDuration, safeAmplifier, ambient);
            }
            if (CTOR_3 != null) {
                return CTOR_3.newInstance(type, safeDuration, safeAmplifier);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to create PotionEffect", ex);
        }
        return new PotionEffect(type, safeDuration, safeAmplifier);
    }

    private static String normalize(String input) {
        String value = input.toLowerCase(Locale.ROOT);
        int colon = value.indexOf(':');
        if (colon >= 0 && colon + 1 < value.length()) {
            value = value.substring(colon + 1);
        }
        value = value.replace(' ', '_').replace('-', '_');
        return value.toUpperCase(Locale.ROOT);
    }

    private static Constructor<PotionEffect> findConstructor(Class<?>... params) {
        try {
            @SuppressWarnings("unchecked")
            Constructor<PotionEffect> ctor = (Constructor<PotionEffect>) PotionEffect.class.getConstructor(params);
            return ctor;
        } catch (Exception ex) {
            return null;
        }
    }

    private static PotionEffectType getByKey(String key) {
        try {
            Class<?> namespacedKeyClass = Class.forName("org.bukkit.NamespacedKey");
            Object keyObj;
            try {
                Method minecraft = namespacedKeyClass.getMethod("minecraft", String.class);
                keyObj = minecraft.invoke(null, key);
            } catch (NoSuchMethodException ex) {
                Constructor<?> ctor = namespacedKeyClass.getConstructor(String.class, String.class);
                keyObj = ctor.newInstance("minecraft", key);
            }
            Method getByKey = PotionEffectType.class.getMethod("getByKey", namespacedKeyClass);
            Object result = getByKey.invoke(null, keyObj);
            return result instanceof PotionEffectType ? (PotionEffectType) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static PotionEffectType getById(String value) {
        if (!isInteger(value)) {
            return null;
        }
        try {
            Method getById = PotionEffectType.class.getMethod("getById", int.class);
            Object result = getById.invoke(null, Integer.parseInt(value));
            return result instanceof PotionEffectType ? (PotionEffectType) result : null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static boolean isInteger(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            if (i == 0 && c == '-') {
                continue;
            }
            if (c < '0' || c > '9') {
                return false;
            }
        }
        return true;
    }
}
