package me.weezard12.powers.internal;

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

    public SimpleAbilityActivationDispatcher(PlayerPowerManager playerPowerManager) {
        this.playerPowerManager = playerPowerManager;
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

        boolean handled = false;
        for (Power power : powers) {
            ActiveAbility mapped = power.getActiveAbility(activation);
            if (mapped != null) {
                handled |= mapped.tryActivate(player, power, activation);
                continue;
            }

            if (activation == AbilityActivation.ULTIMATE && power.getUltimateAbility() != null) {
                handled |= power.getUltimateAbility().tryActivate(player, power, activation);
                continue;
            }

            for (Ability ability : power.getAbilities()) {
                if (ability instanceof ActiveAbility) {
                    ActiveAbility active = (ActiveAbility) ability;
                    handled |= active.tryActivate(player, power, activation);
                }
            }
        }
        return handled;
    }
}
