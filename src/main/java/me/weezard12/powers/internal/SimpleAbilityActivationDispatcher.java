package me.weezard12.powers.internal;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.AbilityActivationDispatcher;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.PlayerPowerManager;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;

public final class SimpleAbilityActivationDispatcher implements AbilityActivationDispatcher {
    private final PlayerPowerManager playerPowerManager;
    private final SimpleAbilityConditionManager conditionManager;

    public SimpleAbilityActivationDispatcher(PlayerPowerManager playerPowerManager,
                                             SimpleAbilityConditionManager conditionManager) {
        this.playerPowerManager = playerPowerManager;
        this.conditionManager = conditionManager;
    }

    @Override
    public boolean dispatch(Player player, AbilityActivation activation) {
        if (player == null || activation == null) {
            return false;
        }
        Set<Power> powers = playerPowerManager.getPowers(player);
        if (powers.isEmpty()) {
            return false;
        }
        conditionManager.handleActivationForPassives(player, powers, activation);

        boolean handled = false;
        for (Power power : powers) {
            Set<ActiveAbility> invoked = Collections.newSetFromMap(new IdentityHashMap<ActiveAbility, Boolean>());

            ActiveAbility mapped = power.getActiveAbility(activation);
            if (mapped != null) {
                handled |= conditionManager.tryActivateConditional(player, power, mapped, activation);
                invoked.add(mapped);
            }

            if (activation == AbilityActivation.ULTIMATE && power.getUltimateAbility() != null) {
                ActiveAbility ultimate = power.getUltimateAbility();
                handled |= conditionManager.tryActivateConditional(player, power, ultimate, activation);
                invoked.add(ultimate);
            }

            for (Ability ability : power.getAbilities()) {
                if (ability instanceof ActiveAbility) {
                    ActiveAbility active = (ActiveAbility) ability;
                    if (invoked.contains(active)) {
                        continue;
                    }
                    handled |= conditionManager.tryActivateConditional(player, power, active, activation);
                }
            }
        }
        return handled;
    }
}
