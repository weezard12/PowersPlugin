package me.weezard12.powers.api.conditions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import me.weezard12.powers.cooldowns.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Wraps any passive ability with condition rules.
 */
public final class ConditionedPassiveAbility implements PassiveAbility, ConditionalAbility {
    private final PassiveAbility delegate;
    private final List<ConditionRule> rules;
    private final boolean defaultEnabled;

    public ConditionedPassiveAbility(PassiveAbility delegate, Collection<ConditionRule> rules) {
        this(delegate, rules, false);
    }

    public ConditionedPassiveAbility(PassiveAbility delegate, Collection<ConditionRule> rules, boolean defaultEnabled) {
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
    public boolean activate(Player player, Power power) {
        return delegate.activate(player, power);
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
    public void onJoin(PlayerJoinEvent event, Player player, Power power) {
        delegate.onJoin(event, player, power);
    }

    @Override
    public void onQuit(PlayerQuitEvent event, Player player, Power power) {
        delegate.onQuit(event, player, power);
    }

    @Override
    public void onDamage(EntityDamageEvent event, Player player, Power power) {
        delegate.onDamage(event, player, power);
    }

    @Override
    public void onDeath(PlayerDeathEvent event, Player player, Power power) {
        delegate.onDeath(event, player, power);
    }

    @Override
    public void onKill(PlayerDeathEvent event, Player killer, Player victim, Power power) {
        delegate.onKill(event, killer, victim, power);
    }

    @Override
    public void onTick(Player player, Power power, long tick) {
        delegate.onTick(player, power, tick);
    }

    @Override
    public long getTickIntervalTicks() {
        return delegate.getTickIntervalTicks();
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
