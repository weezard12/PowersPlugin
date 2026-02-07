package me.weezard12.powers.api.conditions;

/**
 * Action to apply when a condition is evaluated.
 */
public enum ConditionAction {
    /**
     * If the condition matches, mark the ability as enabled.
     */
    ENABLE,

    /**
     * If the condition matches, mark the ability as disabled.
     */
    DISABLE,

    /**
     * If the condition matches, trigger active ability activation.
     */
    TRIGGER,

    /**
     * Set enabled state to the current match result (true or false).
     */
    SYNC
}
