package me.weezard12.powers.api.builtin;

import java.util.Collection;
import java.util.Set;
import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Passive ability that subtracts a flat amount from incoming damage.
 */
public final class DamageShieldPassiveAbility implements PassiveAbility {
    private final String id;
    private final String name;
    private final double reduction;
    private final double minDamage;
    private final Set<EntityDamageEvent.DamageCause> causes;

    public DamageShieldPassiveAbility(String id, String name, double reduction) {
        this(id, name, reduction, null, 0.0);
    }

    public DamageShieldPassiveAbility(String id, String name, double reduction,
                                      Collection<EntityDamageEvent.DamageCause> causes) {
        this(id, name, reduction, causes, 0.0);
    }

    public DamageShieldPassiveAbility(String id, String name, double reduction,
                                      Collection<EntityDamageEvent.DamageCause> causes, double minDamage) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.reduction = BuiltinAbilityUtils.clampMin(reduction, 0.0);
        this.minDamage = BuiltinAbilityUtils.clampMin(minDamage, 0.0);
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

    public double getReduction() {
        return reduction;
    }

    public double getMinDamage() {
        return minDamage;
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
        double newDamage = event.getDamage() - reduction;
        if (Double.isNaN(newDamage)) {
            newDamage = minDamage;
        }
        if (newDamage < minDamage) {
            newDamage = minDamage;
        }
        event.setDamage(newDamage);
    }
}
