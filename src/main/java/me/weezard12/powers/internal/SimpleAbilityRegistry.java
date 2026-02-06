package me.weezard12.powers.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.AbilityRegistry;

public final class SimpleAbilityRegistry implements AbilityRegistry {
    private final Map<String, Ability> abilities = new ConcurrentHashMap<String, Ability>();

    @Override
    public boolean registerAbility(Ability ability) {
        if (ability == null) {
            return false;
        }
        String id = normalizeId(ability.getId());
        if (id == null) {
            return false;
        }
        return abilities.putIfAbsent(id, ability) == null;
    }

    @Override
    public boolean unregisterAbility(String id) {
        String normalized = normalizeId(id);
        if (normalized == null) {
            return false;
        }
        return abilities.remove(normalized) != null;
    }

    @Override
    public Ability getAbility(String id) {
        String normalized = normalizeId(id);
        if (normalized == null) {
            return null;
        }
        return abilities.get(normalized);
    }

    @Override
    public Collection<Ability> getAbilities() {
        return Collections.unmodifiableCollection(abilities.values());
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
