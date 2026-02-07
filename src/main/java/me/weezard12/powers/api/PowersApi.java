package me.weezard12.powers.api;

import me.weezard12.powers.api.conditions.AbilityConditionManager;

public interface PowersApi {
    AbilityRegistry getAbilityRegistry();

    PowerRegistry getPowerRegistry();

    PlayerPowerManager getPlayerPowerManager();

    AbilityActivationDispatcher getActivationDispatcher();

    PowerRarityRegistry getPowerRarityRegistry();

    default AbilityConditionManager getAbilityConditionManager() {
        return null;
    }
}
