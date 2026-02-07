package me.weezard12.powers.internal;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.Power;
import me.weezard12.powers.api.conditions.AbilityConditionManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SimplePlayerPowerManager implements PlayerPowerManager {
    private final ConcurrentHashMap<UUID, Set<Power>> playerPowers = new ConcurrentHashMap<UUID, Set<Power>>();
    private final Logger logger;
    private volatile AbilityConditionManager conditionManager;

    public SimplePlayerPowerManager(Logger logger) {
        this(logger, null);
    }

    public SimplePlayerPowerManager(Logger logger, AbilityConditionManager conditionManager) {
        this.logger = logger;
        this.conditionManager = conditionManager;
    }

    public void setAbilityConditionManager(AbilityConditionManager conditionManager) {
        this.conditionManager = conditionManager;
    }

    @Override
    public boolean addPower(Player player, Power power) {
        if (player == null || power == null) {
            return false;
        }
        Set<Power> powers = getOrCreate(player.getUniqueId());
        if (!powers.add(power)) {
            return false;
        }
        for (Ability ability : power.getAbilities()) {
            try {
                ability.onGrant(player, power);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Ability onGrant failed: " + ability.getId(), ex);
            }
        }
        AbilityConditionManager manager = conditionManager;
        if (manager != null) {
            manager.onPowerAdded(player, power);
        }
        return true;
    }

    @Override
    public boolean removePower(Player player, Power power) {
        if (player == null || power == null) {
            return false;
        }
        Set<Power> powers = playerPowers.get(player.getUniqueId());
        if (powers == null || !powers.remove(power)) {
            return false;
        }
        for (Ability ability : power.getAbilities()) {
            try {
                ability.onRevoke(player, power);
            } catch (Exception ex) {
                logger.log(Level.SEVERE, "Ability onRevoke failed: " + ability.getId(), ex);
            }
        }
        AbilityConditionManager manager = conditionManager;
        if (manager != null) {
            manager.onPowerRemoved(player, power);
        }
        return true;
    }

    @Override
    public boolean hasPower(Player player, Power power) {
        if (player == null || power == null) {
            return false;
        }
        return hasPower(player.getUniqueId(), power);
    }

    @Override
    public boolean hasPower(UUID playerId, Power power) {
        if (playerId == null || power == null) {
            return false;
        }
        Set<Power> powers = playerPowers.get(playerId);
        return powers != null && powers.contains(power);
    }

    @Override
    public Set<Power> getPowers(Player player) {
        if (player == null) {
            return Collections.emptySet();
        }
        return getPowers(player.getUniqueId());
    }

    @Override
    public Set<Power> getPowers(UUID playerId) {
        if (playerId == null) {
            return Collections.emptySet();
        }
        Set<Power> powers = playerPowers.get(playerId);
        if (powers == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(powers);
    }

    @Override
    public void clearPowers(Player player) {
        if (player == null) {
            return;
        }
        clearPowers(player.getUniqueId(), player);
    }

    @Override
    public void clearPowers(UUID playerId) {
        Player player = playerId == null ? null : Bukkit.getPlayer(playerId);
        clearPowers(playerId, player);
    }

    private void clearPowers(UUID playerId, Player player) {
        if (playerId == null) {
            return;
        }
        Set<Power> powers = playerPowers.remove(playerId);
        if (powers == null) {
            return;
        }
        AbilityConditionManager manager = conditionManager;
        if (manager != null) {
            if (player != null) {
                manager.onPowersCleared(player);
            } else {
                manager.clearPlayerState(playerId);
            }
        }
        if (player == null) {
            return;
        }
        for (Power power : powers) {
            for (Ability ability : power.getAbilities()) {
                try {
                    ability.onRevoke(player, power);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Ability onRevoke failed: " + ability.getId(), ex);
                }
            }
        }
    }

    private Set<Power> getOrCreate(UUID playerId) {
        Set<Power> powers = playerPowers.get(playerId);
        if (powers != null) {
            return powers;
        }
        Set<Power> created = Collections.newSetFromMap(new ConcurrentHashMap<Power, Boolean>());
        Set<Power> existing = playerPowers.putIfAbsent(playerId, created);
        return existing == null ? created : existing;
    }
}
