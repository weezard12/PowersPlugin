package me.weezard12.powers.api.builtin;

import java.util.Collection;
import java.util.Set;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Passive ability that multiplies incoming damage for selected causes.
 */
public final class DamageMultiplierPassiveAbility implements PassiveAbility {
    private final String id;
    private final String name;
    private final double multiplier;
    private final Set<EntityDamageEvent.DamageCause> causes;

    public DamageMultiplierPassiveAbility(String id, String name, double multiplier) {
        this(id, name, multiplier, null);
    }

    public DamageMultiplierPassiveAbility(String id, String name, double multiplier,
                                          Collection<EntityDamageEvent.DamageCause> causes) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.multiplier = BuiltinAbilityUtils.clampMin(multiplier, 0.0);
        this.causes = BuiltinAbilityUtils.copyCauses(causes);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public Set<EntityDamageEvent.DamageCause> getCauses() {
        return causes;
    }

    @Override
    public void onDamage(EntityDamageEvent event, Player player, Power power) {
        if (event == null || event.isCancelled()) {
            return;
        }
        if (!BuiltinAbilityUtils.matchesCause(causes, event.getCause())) {
            return;
        }
        double baseDamage = event.getDamage();
        double newDamage = baseDamage * multiplier;
        if (Double.isNaN(newDamage) || newDamage < 0.0) {
            newDamage = 0.0;
        }
        event.setDamage(newDamage);
    }
}
