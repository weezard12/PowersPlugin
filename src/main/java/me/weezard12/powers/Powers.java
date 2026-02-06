package me.weezard12.powers;

import me.weezard12.powers.api.AbilityActivationDispatcher;
import me.weezard12.powers.api.AbilityRegistry;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.PowerRarity;
import me.weezard12.powers.api.PowerRarityRegistry;
import me.weezard12.powers.api.PowerRegistry;
import me.weezard12.powers.api.PowersApi;
import me.weezard12.powers.cooldowns.CooldownManager;
import me.weezard12.powers.internal.AbilityActivationListener;
import me.weezard12.powers.internal.PassiveAbilityRouter;
import me.weezard12.powers.internal.SimpleAbilityActivationDispatcher;
import me.weezard12.powers.internal.SimpleAbilityRegistry;
import me.weezard12.powers.internal.SimplePlayerPowerManager;
import me.weezard12.powers.internal.SimplePowerRegistry;
import me.weezard12.powers.internal.SimplePowerRarityRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Powers extends JavaPlugin implements PowersApi {
    private AbilityRegistry abilityRegistry;
    private PowerRegistry powerRegistry;
    private PlayerPowerManager playerPowerManager;
    private AbilityActivationDispatcher activationDispatcher;
    private PowerRarityRegistry powerRarityRegistry;
    private PassiveAbilityRouter passiveAbilityRouter;

    @Override
    public void onEnable() {
        abilityRegistry = new SimpleAbilityRegistry();
        powerRegistry = new SimplePowerRegistry();
        playerPowerManager = new SimplePlayerPowerManager(getLogger());
        activationDispatcher = new SimpleAbilityActivationDispatcher(playerPowerManager);
        powerRarityRegistry = new SimplePowerRarityRegistry();
        registerDefaultRarities(powerRarityRegistry);
        CooldownManager.init(this);
        passiveAbilityRouter = new PassiveAbilityRouter(playerPowerManager, getLogger());
        Bukkit.getServicesManager().register(PowersApi.class, this, this, ServicePriority.Normal);
        Bukkit.getPluginManager().registerEvents(new AbilityActivationListener(activationDispatcher), this);
        Bukkit.getPluginManager().registerEvents(passiveAbilityRouter, this);
        Bukkit.getScheduler().runTaskTimer(this, passiveAbilityRouter, 1L, 1L);
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

    @Override
    public AbilityActivationDispatcher getActivationDispatcher() {
        return activationDispatcher;
    }

    @Override
    public PowerRarityRegistry getPowerRarityRegistry() {
        return powerRarityRegistry;
    }

    private void registerDefaultRarities(PowerRarityRegistry registry) {
        registry.register(new PowerRarity("Common", ChatColor.WHITE, 1));
        registry.register(new PowerRarity("Rare", ChatColor.AQUA, 2));
        registry.register(new PowerRarity("Legendary", ChatColor.GOLD, 3));
        registry.register(new PowerRarity("Mythic", ChatColor.LIGHT_PURPLE, 4));
        registry.setDefault("Common");
    }
}
