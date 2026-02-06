package me.weezard12.powers.api;

import java.util.Collection;

public interface PowerRarityRegistry {
    boolean register(PowerRarity rarity);

    boolean unregister(String name);

    PowerRarity get(String name);

    Collection<PowerRarity> getAll();

    PowerRarity getDefault();

    void setDefault(PowerRarity rarity);

    boolean setDefault(String name);
}
