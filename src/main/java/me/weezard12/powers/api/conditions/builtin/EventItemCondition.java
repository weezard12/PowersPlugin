package me.weezard12.powers.api.conditions.builtin;

import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;
import me.weezard12.powers.api.conditions.ConditionTrigger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Matches drop/pickup conditions against the event item.
 */
public final class EventItemCondition implements AbilityCondition {
    private final ConditionTrigger trigger;
    private final ItemMatcher matcher;

    public EventItemCondition(ConditionTrigger trigger, ItemMatcher matcher) {
        if (trigger != ConditionTrigger.DROP && trigger != ConditionTrigger.PICK_UP) {
            throw new IllegalArgumentException("trigger must be DROP or PICK_UP");
        }
        if (matcher == null) {
            throw new IllegalArgumentException("matcher cannot be null");
        }
        this.trigger = trigger;
        this.matcher = matcher;
    }

    @Override
    public boolean supports(String triggerKey) {
        ConditionTrigger incoming = ConditionTrigger.fromString(triggerKey);
        return incoming == trigger;
    }

    @Override
    public boolean supports(ConditionTrigger incomingTrigger) {
        return incomingTrigger == trigger;
    }

    @Override
    public boolean matches(AbilityConditionContext context) {
        if (context == null || context.getTrigger() != trigger) {
            return false;
        }
        ItemStack eventItem = context.getEventItem();
        return eventItem != null && eventItem.getType() != Material.AIR && matcher.matches(eventItem);
    }
}
