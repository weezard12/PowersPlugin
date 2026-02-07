package me.weezard12.powers.api.conditions;

/**
 * A condition + action pair.
 */
public final class ConditionRule {
    private final AbilityCondition condition;
    private final ConditionAction action;

    public ConditionRule(AbilityCondition condition, ConditionAction action) {
        if (condition == null) {
            throw new IllegalArgumentException("condition cannot be null");
        }
        if (action == null) {
            throw new IllegalArgumentException("action cannot be null");
        }
        this.condition = condition;
        this.action = action;
    }

    public static ConditionRule of(AbilityCondition condition, ConditionAction action) {
        return new ConditionRule(condition, action);
    }

    public AbilityCondition getCondition() {
        return condition;
    }

    public ConditionAction getAction() {
        return action;
    }
}
