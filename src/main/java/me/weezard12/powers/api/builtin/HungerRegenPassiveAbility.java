package me.weezard12.powers.api.builtin;

import me.weezard12.powers.api.PassiveAbility;
import me.weezard12.powers.api.Power;
import org.bukkit.entity.Player;

/**
 * Passive ability that regenerates food level and saturation over time.
 */
public final class HungerRegenPassiveAbility implements PassiveAbility {
    private static final long DEFAULT_INTERVAL_TICKS = 40L;

    private final String id;
    private final String name;
    private final int foodAmount;
    private final float saturationAmount;
    private final long tickIntervalTicks;

    public HungerRegenPassiveAbility(String id, String name, int foodAmount, float saturationAmount) {
        this(id, name, foodAmount, saturationAmount, DEFAULT_INTERVAL_TICKS);
    }

    public HungerRegenPassiveAbility(String id, String name, int foodAmount, float saturationAmount,
                                     long tickIntervalTicks) {
        this.id = BuiltinAbilityUtils.requireNonEmpty(id, "id");
        this.name = BuiltinAbilityUtils.requireNonEmpty(name, "name");
        this.foodAmount = foodAmount;
        this.saturationAmount = saturationAmount;
        this.tickIntervalTicks = BuiltinAbilityUtils.normalizeInterval(tickIntervalTicks, DEFAULT_INTERVAL_TICKS);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public int getFoodAmount() {
        return foodAmount;
    }

    public float getSaturationAmount() {
        return saturationAmount;
    }

    @Override
    public long getTickIntervalTicks() {
        return tickIntervalTicks;
    }

    @Override
    public void onTick(Player player, Power power, long tick) {
        if (player == null || !player.isOnline()) {
            return;
        }
        if (foodAmount == 0 && saturationAmount == 0.0f) {
            return;
        }
        int currentFood = player.getFoodLevel();
        int newFood = BuiltinAbilityUtils.clampInt(currentFood + foodAmount, 0, 20);
        if (newFood != currentFood) {
            player.setFoodLevel(newFood);
        }

        float currentSaturation = player.getSaturation();
        float targetSaturation = currentSaturation + saturationAmount;
        if (Float.isNaN(targetSaturation)) {
            return;
        }
        if (targetSaturation < 0.0f) {
            targetSaturation = 0.0f;
        }
        int saturationCap = newFood;
        if (targetSaturation > saturationCap) {
            targetSaturation = saturationCap;
        }
        if (targetSaturation != currentSaturation) {
            player.setSaturation(targetSaturation);
        }
    }
}
