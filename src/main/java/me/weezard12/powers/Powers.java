package me.weezard12.powers;

import me.weezard12.powers.api.AbilityRegistry;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.PowerRegistry;
import me.weezard12.powers.api.PowersApi;
import me.weezard12.powers.cooldowns.CooldownManager;
import me.weezard12.powers.internal.SimpleAbilityRegistry;
import me.weezard12.powers.internal.SimplePlayerPowerManager;
import me.weezard12.powers.internal.SimplePowerRegistry;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Powers extends JavaPlugin implements PowersApi {
    private AbilityRegistry abilityRegistry;
    private PowerRegistry powerRegistry;
    private PlayerPowerManager playerPowerManager;

    @Override
    public void onEnable() {
        abilityRegistry = new SimpleAbilityRegistry();
        powerRegistry = new SimplePowerRegistry();
        playerPowerManager = new SimplePlayerPowerManager(getLogger());
        CooldownManager.init(this);
        Bukkit.getServicesManager().register(PowersApi.class, this, this, ServicePriority.Normal);
    }

    @Override
    public void onDisable() {
        Bukkit.getServicesManager().unregister(PowersApi.class, this);
    }

    @Override
    public AbilityRegistry getAbilityRegistry() {
        return abilityRegistry;
    }

    @Override
    public PowerRegistry getPowerRegistry() {
        return powerRegistry;
    }

    @Override
    public PlayerPowerManager getPlayerPowerManager() {
        return playerPowerManager;
    }
}
