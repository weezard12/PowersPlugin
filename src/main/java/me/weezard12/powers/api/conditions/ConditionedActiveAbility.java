package me.weezard12.powers.api.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.ActiveAbility;
import me.weezard12.powers.api.Power;
import me.weezard12.powers.cooldowns.Cooldown;
import org.bukkit.entity.Player;

/**
 * Wraps any active ability with condition rules.
 */
public final class ConditionedActiveAbility implements ActiveAbility, ConditionalAbility {
    private final ActiveAbility delegate;
    private final List<ConditionRule> rules;
    private final boolean defaultEnabled;

    public ConditionedActiveAbility(ActiveAbility delegate, Collection<ConditionRule> rules) {
        this(delegate, rules, false);
    }

    public ConditionedActiveAbility(ActiveAbility delegate, Collection<ConditionRule> rules, boolean defaultEnabled) {
        if (delegate == null) {
            throw new IllegalArgumentException("delegate cannot be null");
        }
        this.delegate = delegate;
        this.rules = immutableRules(rules);
        this.defaultEnabled = defaultEnabled;
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public List<String> getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getIconKey() {
        return delegate.getIconKey();
    }

    @Override
    public void onGrant(Player player, Power power) {
        delegate.onGrant(player, power);
    }

    @Override
    public void onRevoke(Player player, Power power) {
        delegate.onRevoke(player, power);
    }

    @Override
    public boolean activate(Player player, Power power, AbilityActivation activation) {
        return delegate.activate(player, power, activation);
    }

    @Override
    public String getCooldownId() {
        return delegate.getCooldownId();
    }

    @Override
    public long getCooldownSeconds(Player player, Power power) {
        return delegate.getCooldownSeconds(player, power);
    }

    @Override
    public boolean hasCooldown(Player player, Power power) {
        return delegate.hasCooldown(player, power);
    }

    @Override
    public Cooldown createCooldown(Player player, Power power) {
        return delegate.createCooldown(player, power);
    }

    @Override
    public List<ConditionRule> getConditionRules() {
        return rules;
    }

    @Override
    public boolean isConditionStateEnabledByDefault(Player player, Power power) {
        return defaultEnabled;
    }

    private static List<ConditionRule> immutableRules(Collection<ConditionRule> rules) {
        if (rules == null || rules.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConditionRule> copy = new ArrayList<ConditionRule>();
        for (ConditionRule rule : rules) {
            if (rule != null) {
                copy.add(rule);
            }
        }
        if (copy.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(copy);
    }
}
