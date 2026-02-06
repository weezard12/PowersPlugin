package me.weezard12.powers.api;

import java.util.Collection;

public interface AbilityRegistry {
    boolean registerAbility(Ability ability);

    boolean unregisterAbility(String id);

    Ability getAbility(String id);

    Collection<Ability> getAbilities();
}
