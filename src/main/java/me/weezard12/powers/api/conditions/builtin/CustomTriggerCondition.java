package me.weezard12.powers.api.conditions.builtin;

import java.util.Locale;
import me.weezard12.powers.api.conditions.AbilityCondition;
import me.weezard12.powers.api.conditions.AbilityConditionContext;

/**
 * Matches an arbitrary trigger key dispatched by plugins.
 */
public final class CustomTriggerCondition implements AbilityCondition {
    private final String triggerKey;

    public CustomTriggerCondition(String triggerKey) {
        this.triggerKey = normalize(triggerKey);
        if (this.triggerKey == null) {
            throw new IllegalArgumentException("triggerKey cannot be null/empty");
        }
    }

    @Override
    public boolean supports(String incomingTriggerKey) {
        String normalized = normalize(incomingTriggerKey);
        return normalized != null && triggerKey.equals(normalized);
    }

    @Override
    public boolean matches(AbilityConditionContext context) {
        if (context == null) {
            return false;
        }
        String incoming = normalize(context.getTriggerKey());
        return incoming != null && triggerKey.equals(incoming);
    }

    private static String normalize(String key) {
        if (key == null) {
            return null;
        }
        String normalized = key.trim();
        if (normalized.isEmpty()) {
            return null;
        }
        return normalized.replace('-', '_').replace(' ', '_').toUpperCase(Locale.ROOT);
    }
}
