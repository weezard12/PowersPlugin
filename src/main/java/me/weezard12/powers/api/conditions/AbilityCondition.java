package me.weezard12.powers.api.conditions;

/**
 * A modular condition evaluated against a runtime context.
 */
public interface AbilityCondition {
    default boolean supports(String triggerKey) {
        if (triggerKey == null || triggerKey.trim().isEmpty()) {
            return false;
        }
        ConditionTrigger trigger = ConditionTrigger.fromString(triggerKey);
        if (trigger != null) {
            return supports(trigger);
        }
        // Unknown keys are treated as custom triggers.
        return true;
    }

    default boolean supports(ConditionTrigger trigger) {
        return true;
    }

    boolean matches(AbilityConditionContext context);
}
