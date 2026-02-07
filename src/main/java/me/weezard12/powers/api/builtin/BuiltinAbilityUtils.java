package me.weezard12.powers.api.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import org.bukkit.event.entity.EntityDamageEvent;

final class BuiltinAbilityUtils {
    private BuiltinAbilityUtils() {
    }

    static String requireNonEmpty(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return trimmed;
    }

    static double clampMin(double value, double min) {
        if (Double.isNaN(value)) {
            return min;
        }
        return value < min ? min : value;
    }

    static float clampMin(float value, float min) {
        if (Float.isNaN(value)) {
            return min;
        }
        return value < min ? min : value;
    }

    static int clampInt(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static long normalizeInterval(long interval, long defaultInterval) {
        return interval <= 0L ? defaultInterval : interval;
    }

    static Set<EntityDamageEvent.DamageCause> copyCauses(Collection<EntityDamageEvent.DamageCause> causes) {
        if (causes == null || causes.isEmpty()) {
            return null;
        }
        EnumSet<EntityDamageEvent.DamageCause> set = EnumSet.noneOf(EntityDamageEvent.DamageCause.class);
        for (EntityDamageEvent.DamageCause cause : causes) {
            if (cause != null) {
                set.add(cause);
            }
        }
        if (set.isEmpty()) {
            return null;
        }
        return Collections.unmodifiableSet(set);
    }

    static boolean matchesCause(Set<EntityDamageEvent.DamageCause> causes, EntityDamageEvent.DamageCause cause) {
        if (causes == null || causes.isEmpty()) {
            return true;
        }
        return cause != null && causes.contains(cause);
    }
}
