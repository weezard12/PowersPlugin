package me.weezard12.powers.api;

import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

public interface PlayerPowerManager {
    boolean addPower(Player player, Power power);

    boolean removePower(Player player, Power power);

    boolean hasPower(Player player, Power power);

    boolean hasPower(UUID playerId, Power power);

    Set<Power> getPowers(Player player);

    Set<Power> getPowers(UUID playerId);

    void clearPowers(Player player);

    void clearPowers(UUID playerId);
}
