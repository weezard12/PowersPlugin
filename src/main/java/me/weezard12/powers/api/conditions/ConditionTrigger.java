package me.weezard12.powers.api.conditions;

import java.util.Locale;

/**
 * Built-in condition triggers used by the condition engine.
 */
public enum ConditionTrigger {
    RIGHT_CLICK,
    SHIFT_RIGHT_CLICK,
    ANY_RIGHT_CLICK,

    LEFT_CLICK,
    SHIFT_LEFT_CLICK,
    ANY_LEFT_CLICK,

    START_HOLD_HAND,
    START_HOLD_OFF_HAND,
    START_HOLD_ANY_HAND,

    FINISH_HOLD_HAND,
    FINISH_HOLD_OFF_HAND,
    FINISH_HOLD_ANY_HAND,

    HOLD_INVENTORY,
    DROP,
    PICK_UP;

    public static ConditionTrigger fromString(String input) {
        if (input == null) {
            return null;
        }
        String normalized = input.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        normalized = normalized.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
        if (normalized.startsWith("ON_")) {
            normalized = normalized.substring(3);
        }
        if ("PICKUP".equals(normalized)) {
            normalized = "PICK_UP";
        }
        try {
            return ConditionTrigger.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
