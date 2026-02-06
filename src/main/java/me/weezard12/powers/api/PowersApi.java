package me.weezard12.powers.api;

public interface PowersApi {
    AbilityRegistry getAbilityRegistry();

    PowerRegistry getPowerRegistry();

    PlayerPowerManager getPlayerPowerManager();

    AbilityActivationDispatcher getActivationDispatcher();

    PowerRarityRegistry getPowerRarityRegistry();
}
