package me.weezard12.powers.api.conditions;

import me.weezard12.powers.api.Ability;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Runtime context provided to conditions.
 */
public final class AbilityConditionContext {
    private final Player player;
    private final Power power;
    private final Ability ability;
    private final ConditionTrigger trigger;
    private final String triggerKey;
    private final AbilityActivation activation;
    private final Object sourceEvent;
    private final ItemStack eventItem;
    private final ItemStack mainHandBefore;
    private final ItemStack offHandBefore;
    private final ItemStack mainHandAfter;
    private final ItemStack offHandAfter;
    private final long tick;

    private AbilityConditionContext(Builder builder) {
        this.player = builder.player;
        this.power = builder.power;
        this.ability = builder.ability;
        this.trigger = builder.trigger;
        this.triggerKey = builder.triggerKey;
        this.activation = builder.activation;
        this.sourceEvent = builder.sourceEvent;
        this.eventItem = builder.eventItem;
        this.mainHandBefore = builder.mainHandBefore;
        this.offHandBefore = builder.offHandBefore;
        this.mainHandAfter = builder.mainHandAfter;
        this.offHandAfter = builder.offHandAfter;
        this.tick = builder.tick;
    }

    public Player getPlayer() {
        return player;
    }

    public Power getPower() {
        return power;
    }

    public Ability getAbility() {
        return ability;
    }

    public ConditionTrigger getTrigger() {
        return trigger;
    }

    public String getTriggerKey() {
        return triggerKey;
    }

    public AbilityActivation getActivation() {
        return activation;
    }

    public Object getSourceEvent() {
        return sourceEvent;
    }

    public ItemStack getEventItem() {
        return eventItem;
    }

    public ItemStack getMainHandBefore() {
        return mainHandBefore;
    }

    public ItemStack getOffHandBefore() {
        return offHandBefore;
    }

    public ItemStack getMainHandAfter() {
        return mainHandAfter;
    }

    public ItemStack getOffHandAfter() {
        return offHandAfter;
    }

    public long getTick() {
        return tick;
    }

    public static Builder builder(Player player, Power power, Ability ability, ConditionTrigger trigger) {
        return new Builder(player, power, ability, trigger, trigger == null ? null : trigger.name());
    }

    public static Builder builder(Player player, Power power, Ability ability, String triggerKey) {
        return new Builder(player, power, ability, null, triggerKey);
    }

    public static final class Builder {
        private final Player player;
        private final Power power;
        private final Ability ability;
        private final ConditionTrigger trigger;
        private final String triggerKey;
        private AbilityActivation activation;
        private Object sourceEvent;
        private ItemStack eventItem;
        private ItemStack mainHandBefore;
        private ItemStack offHandBefore;
        private ItemStack mainHandAfter;
        private ItemStack offHandAfter;
        private long tick;

        private Builder(Player player, Power power, Ability ability, ConditionTrigger trigger, String triggerKey) {
            this.player = player;
            this.power = power;
            this.ability = ability;
            this.trigger = trigger;
            this.triggerKey = triggerKey;
        }

        public Builder setActivation(AbilityActivation activation) {
            this.activation = activation;
            return this;
        }

        public Builder setSourceEvent(Object sourceEvent) {
            this.sourceEvent = sourceEvent;
            return this;
        }

        public Builder setEventItem(ItemStack eventItem) {
            this.eventItem = eventItem;
            return this;
        }

        public Builder setMainHandBefore(ItemStack mainHandBefore) {
            this.mainHandBefore = mainHandBefore;
            return this;
        }

        public Builder setOffHandBefore(ItemStack offHandBefore) {
            this.offHandBefore = offHandBefore;
            return this;
        }

        public Builder setMainHandAfter(ItemStack mainHandAfter) {
            this.mainHandAfter = mainHandAfter;
            return this;
        }

        public Builder setOffHandAfter(ItemStack offHandAfter) {
            this.offHandAfter = offHandAfter;
            return this;
        }

        public Builder setTick(long tick) {
            this.tick = tick;
            return this;
        }

        public AbilityConditionContext build() {
            return new AbilityConditionContext(this);
        }
    }
}
