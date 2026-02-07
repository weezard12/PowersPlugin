package me.weezard12.powers.api.conditions.builtin;

import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;
import me.weezard12.powers.api.conditions.ConditionTrigger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

/**
 * Matches start/finish hold transitions for the configured trigger.
 */
public final class ItemHoldTransitionCondition implements AbilityCondition {
    private final ConditionTrigger trigger;
    private final ItemMatcher matcher;

    public ItemHoldTransitionCondition(ConditionTrigger trigger, ItemMatcher matcher) {
        if (trigger == null) {
            throw new IllegalArgumentException("trigger cannot be null");
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
        switch (trigger) {
            case START_HOLD_HAND:
                return !matches(context.getMainHandBefore()) && matches(context.getMainHandAfter());
            case START_HOLD_OFF_HAND:
                return !matches(context.getOffHandBefore()) && matches(context.getOffHandAfter());
            case START_HOLD_ANY_HAND:
                return !matchesAny(context.getMainHandBefore(), context.getOffHandBefore())
                        && matchesAny(context.getMainHandAfter(), context.getOffHandAfter());
            case FINISH_HOLD_HAND:
                return matches(context.getMainHandBefore()) && !matches(context.getMainHandAfter());
            case FINISH_HOLD_OFF_HAND:
                return matches(context.getOffHandBefore()) && !matches(context.getOffHandAfter());
            case FINISH_HOLD_ANY_HAND:
                return matchesAny(context.getMainHandBefore(), context.getOffHandBefore())
                        && !matchesAny(context.getMainHandAfter(), context.getOffHandAfter());
            default:
                return false;
        }
    }

    private boolean matchesAny(ItemStack first, ItemStack second) {
        return matches(first) || matches(second);
    }

    private boolean matches(ItemStack itemStack) {
        return !isAir(itemStack) && matcher.matches(itemStack);
    }

    private static boolean isAir(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }
}
