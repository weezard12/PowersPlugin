package me.weezard12.powers.internal;

import java.lang.reflect.Method;
import me.weezard12.powers.api.AbilityActivation;
import me.weezard12.powers.api.AbilityActivationDispatcher;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public final class AbilityActivationListener implements Listener {
    private final AbilityActivationDispatcher dispatcher;

    public AbilityActivationListener(AbilityActivationDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!isMainHand(event)) {
            return;
        }
        AbilityActivation activation = toActivation(event);
        if (activation == AbilityActivation.UNKNOWN) {
            return;
        }

        Player player = event.getPlayer();
        dispatcher.dispatch(player, activation);
    }

    private static AbilityActivation toActivation(PlayerInteractEvent event) {
        Action action = event.getAction();
        boolean sneaking = event.getPlayer().isSneaking();
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            return sneaking ? AbilityActivation.SHIFT_RIGHT_CLICK : AbilityActivation.RIGHT_CLICK;
        }
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            return sneaking ? AbilityActivation.SHIFT_LEFT_CLICK : AbilityActivation.LEFT_CLICK;
        }
        return AbilityActivation.UNKNOWN;
    }

    private static boolean isMainHand(PlayerInteractEvent event) {
        try {
            Method getHand = event.getClass().getMethod("getHand");
            Object hand = getHand.invoke(event);
            if (hand == null) {
                return true;
            }
            Class<?> enumClass = Class.forName("org.bukkit.inventory.EquipmentSlot");
            Object main = Enum.valueOf(enumClass.asSubclass(Enum.class), "HAND");
            return hand.equals(main);
        } catch (Exception ignored) {
            // Pre-1.9 or no offhand support.
            return true;
        }
    }
}
