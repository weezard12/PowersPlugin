package me.weezard12.powers.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.weezard12.powers.api.Power;
import me.weezard12.powers.api.PowerRegistry;

public final class SimplePowerRegistry implements PowerRegistry {
    private final Map<String, Power> powers = new ConcurrentHashMap<String, Power>();

    @Override
    public boolean registerPower(Power power) {
        if (power == null) {
            return false;
        }
        String id = normalizeId(power.getId());
        if (id == null) {
            return false;
        }
        return powers.putIfAbsent(id, power) == null;
    }

    @Override
    public boolean unregisterPower(String id) {
        String normalized = normalizeId(id);
        if (normalized == null) {
            return false;
        }
        return powers.remove(normalized) != null;
    }

    @Override
    public Power getPower(String id) {
        String normalized = normalizeId(id);
        if (normalized == null) {
            return null;
        }
        return powers.get(normalized);
    }

    @Override
    public Collection<Power> getPowers() {
        return Collections.unmodifiableCollection(powers.values());
    }

    private static String normalizeId(String id) {
        if (id == null) {
            return null;
        }
        String trimmed = id.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
