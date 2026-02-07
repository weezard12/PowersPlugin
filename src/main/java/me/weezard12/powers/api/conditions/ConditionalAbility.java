package me.weezard12.powers.api.conditions;

import java.util.Collections;
import java.util.List;
import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;

/**
 * Optional extension point for abilities that are controlled by condition rules.
 */
public interface ConditionalAbility extends Ability {

    /**
     * Ordered condition rules applied for this ability.
     */
    default List<ConditionRule> getConditionRules() {
        return Collections.emptyList();
    }

    /**
     * Initial enabled state for abilities with state rules (ENABLE, DISABLE, SYNC).
     */
    default boolean isConditionStateEnabledByDefault(Player player, Power power) {
        return false;
    }
}
