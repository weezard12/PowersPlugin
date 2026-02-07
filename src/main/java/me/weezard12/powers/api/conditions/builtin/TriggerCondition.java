package me.weezard12.powers.api.conditions.builtin;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;
import me.weezard12.powers.api.conditions.ConditionTrigger;

/**
 * Matches one or more condition triggers.
 */
public final class TriggerCondition implements AbilityCondition {
    private final Set<ConditionTrigger> triggers;

    public TriggerCondition(ConditionTrigger trigger) {
        this(Collections.singleton(trigger));
    }

    public TriggerCondition(Set<ConditionTrigger> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            throw new IllegalArgumentException("triggers cannot be empty");
        }
        EnumSet<ConditionTrigger> copy = EnumSet.noneOf(ConditionTrigger.class);
        for (ConditionTrigger trigger : triggers) {
            if (trigger != null) {
                copy.add(trigger);
            }
        }
        if (copy.isEmpty()) {
            throw new IllegalArgumentException("triggers cannot be empty");
        }
        this.triggers = Collections.unmodifiableSet(copy);
    }

    @Override
    public boolean supports(String triggerKey) {
        ConditionTrigger trigger = ConditionTrigger.fromString(triggerKey);
        return trigger != null && triggers.contains(trigger);
    }

    @Override
    public boolean matches(AbilityConditionContext context) {
        if (context == null) {
            return false;
        }
        if (context.getTrigger() != null && triggers.contains(context.getTrigger())) {
            return true;
        }
        ConditionTrigger byKey = ConditionTrigger.fromString(context.getTriggerKey());
        return byKey != null && triggers.contains(byKey);
    }
}
