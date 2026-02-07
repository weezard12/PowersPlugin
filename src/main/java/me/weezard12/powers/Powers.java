package me.weezard12.powers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import me.weezard12.powers.api.AbilityActivationDispatcher;
import me.weezard12.powers.api.AbilityRegistry;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.PowerRarity;
import me.weezard12.powers.api.PowerRarityRegistry;
import me.weezard12.powers.api.PowerRegistry;
import me.weezard12.powers.api.PowersApi;
import me.weezard12.powers.api.conditions.AbilityConditionManager;
import me.weezard12.powers.cooldowns.CooldownManager;
import me.weezard12.powers.internal.SimpleAbilityConditionManager;
import me.weezard12.powers.internal.SimpleAbilityRegistry;
import me.weezard12.powers.internal.SimplePlayerPowerManager;
import me.weezard12.powers.internal.SimplePowerRegistry;
import me.weezard12.powers.internal.SimplePowerRarityRegistry;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public final class Powers extends JavaPlugin implements PowersApi {
    private AbilityRegistry abilityRegistry;
    private PowerRegistry powerRegistry;
    private PlayerPowerManager playerPowerManager;
    private AbilityActivationDispatcher activationDispatcher;
    private PowerRarityRegistry powerRarityRegistry;
    private SimpleAbilityConditionManager abilityConditionManager;

    @Override
    public void onEnable() {
        abilityRegistry = new SimpleAbilityRegistry();
        powerRegistry = new SimplePowerRegistry();
        SimplePlayerPowerManager manager = new SimplePlayerPowerManager(getLogger());
        playerPowerManager = manager;
        abilityConditionManager = new SimpleAbilityConditionManager(this, playerPowerManager, getLogger());
        manager.setAbilityConditionManager(abilityConditionManager);
        activationDispatcher = abilityConditionManager;
        powerRarityRegistry = new SimplePowerRarityRegistry();
        registerDefaultRarities(powerRarityRegistry);
        CooldownManager.init(this);
        Bukkit.getServicesManager().register(PowersApi.class, this, this, ServicePriority.Normal);
        Bukkit.getPluginManager().registerEvents(abilityConditionManager, this);
        Bukkit.getScheduler().runTaskTimer(this, abilityConditionManager, 1L, 1L);
    }

    @Override
    public void onDisable() {
        for (Player player : getOnlinePlayers()) {
            if (playerPowerManager != null) {
                playerPowerManager.clearPowers(player);
            }
            if (abilityConditionManager != null) {
                abilityConditionManager.clearPlayerState(player);
            }
        }
        Bukkit.getScheduler().cancelTasks(this);
        CooldownManager.clearAll();
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

    @Override
    public AbilityConditionManager getAbilityConditionManager() {
        return abilityConditionManager;
    }

    private void registerDefaultRarities(PowerRarityRegistry registry) {
        registry.register(new PowerRarity("Common", ChatColor.WHITE, 1));
        registry.register(new PowerRarity("Rare", ChatColor.AQUA, 2));
        registry.register(new PowerRarity("Legendary", ChatColor.GOLD, 3));
        registry.register(new PowerRarity("Mythic", ChatColor.LIGHT_PURPLE, 4));
        registry.setDefault("Common");
    }

    private Iterable<Player> getOnlinePlayers() {
        try {
            Method method = Bukkit.class.getMethod("getOnlinePlayers");
            Object result = method.invoke(null);
            if (result instanceof Player[]) {
                return Arrays.asList((Player[]) result);
            }
            if (result instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Player> players = (Collection<Player>) result;
                return players;
            }
        } catch (Exception ex) {
            getLogger().warning("Failed to enumerate online players during shutdown.");
        }
        return Collections.emptyList();
    }
}
