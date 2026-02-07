package me.weezard12.powers.internal;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.Power;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PassiveAbilityRouter implements Listener, Runnable {
    private final PlayerPowerManager playerPowerManager;
    private final SimpleAbilityConditionManager conditionManager;
    private final Logger logger;
    private long tickCounter = 0L;

    public PassiveAbilityRouter(PlayerPowerManager playerPowerManager, SimpleAbilityConditionManager conditionManager,
                                Logger logger) {
        this.playerPowerManager = playerPowerManager;
        this.conditionManager = conditionManager;
        this.logger = logger;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        dispatch(player, (ability, power) -> ability.onJoin(event, player, power));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        dispatch(player, (ability, power) -> ability.onQuit(event, player, power));
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        dispatch(player, (ability, power) -> ability.onDamage(event, player, power));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        dispatch(victim, (ability, power) -> ability.onDeath(event, victim, power));

        Player killer = victim.getKiller();
        if (killer != null && killer.isOnline()) {
            dispatch(killer, (ability, power) -> ability.onKill(event, killer, victim, power));
        }
    }

    @Override
    public void run() {
        tickCounter++;
        for (Player player : getOnlinePlayers()) {
            dispatchTick(player, tickCounter);
        }
    }

    private void dispatchTick(Player player, long tick) {
        if (player == null) {
            return;
        }
        Set<Power> powers = playerPowerManager.getPowers(player);
        if (powers.isEmpty()) {
            return;
        }
        for (Power power : powers) {
            for (Ability ability : power.getPassiveAbilities()) {
                if (!(ability instanceof PassiveAbility)) {
                    continue;
                }
                PassiveAbility passive = (PassiveAbility) ability;
                if (!conditionManager.isPassiveEnabled(player, power, passive)) {
                    continue;
                }
                long interval = passive.getTickIntervalTicks();
                if (interval <= 0L || (tick % interval) != 0L) {
                    continue;
                }
                try {
                    passive.onTick(player, power, tick);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Passive ability onTick failed: " + ability.getId(), ex);
                }
            }
        }
    }

    private void dispatch(Player player, PassiveDispatch dispatch) {
        if (player == null) {
            return;
        }
        Set<Power> powers = playerPowerManager.getPowers(player);
        if (powers.isEmpty()) {
            return;
        }
        for (Power power : powers) {
            for (Ability ability : power.getPassiveAbilities()) {
                if (!(ability instanceof PassiveAbility)) {
                    continue;
                }
                try {
                    PassiveAbility passive = (PassiveAbility) ability;
                    if (!conditionManager.isPassiveEnabled(player, power, passive)) {
                        continue;
                    }
                    dispatch.apply(passive, power);
                } catch (Exception ex) {
                    logger.log(Level.SEVERE, "Passive ability handler failed: " + ability.getId(), ex);
                }
            }
        }
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
                Collection<Player> collection = (Collection<Player>) result;
                return collection;
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to enumerate online players for passive ticks.", ex);
        }
        return Collections.emptyList();
    }

    private interface PassiveDispatch {
        void apply(PassiveAbility ability, Power power);
    }
}
