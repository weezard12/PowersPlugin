package me.weezard12.powers.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.weezard12.powers.api.PowerRarity;
import me.weezard12.powers.api.PowerRarityRegistry;

public final class SimplePowerRarityRegistry implements PowerRarityRegistry {
    private final Map<String, PowerRarity> rarities = new ConcurrentHashMap<String, PowerRarity>();
    private volatile PowerRarity defaultRarity = PowerRarity.getDefault();

    @Override
    public boolean register(PowerRarity rarity) {
        if (rarity == null) {
            return false;
        }
        String id = normalizeId(rarity.getName());
        if (id == null) {
            return false;
        }
        boolean added = rarities.putIfAbsent(id, rarity) == null;
        if (defaultRarity == null) {
            defaultRarity = rarity;
            PowerRarity.setDefault(rarity);
        }
        return added;
    }

    @Override
    public boolean unregister(String name) {
        String id = normalizeId(name);
        if (id == null) {
            return false;
        }
        PowerRarity removed = rarities.remove(id);
        if (removed != null && removed.equals(defaultRarity)) {
            defaultRarity = PowerRarity.getDefault();
        }
        return removed != null;
    }

    @Override
    public PowerRarity get(String name) {
        String id = normalizeId(name);
        if (id == null) {
            return null;
        }
        return rarities.get(id);
    }

    @Override
    public Collection<PowerRarity> getAll() {
        return Collections.unmodifiableCollection(rarities.values());
    }

    @Override
    public PowerRarity getDefault() {
        return defaultRarity != null ? defaultRarity : PowerRarity.getDefault();
    }

    @Override
    public void setDefault(PowerRarity rarity) {
        if (rarity == null) {
            throw new IllegalArgumentException("rarity cannot be null");
        }
        register(rarity);
        defaultRarity = rarity;
        PowerRarity.setDefault(rarity);
    }

    @Override
    public boolean setDefault(String name) {
        PowerRarity rarity = get(name);
        if (rarity == null) {
            return false;
        }
        setDefault(rarity);
        return true;
    }

    private static String normalizeId(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toLowerCase(Locale.ROOT);
    }
}
