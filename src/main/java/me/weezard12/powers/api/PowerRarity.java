package me.weezard12.powers.api;

import java.util.Locale;
import java.util.Objects;
import org.bukkit.ChatColor;

/**
 * Runtime-configurable rarity definition.
 */
public final class PowerRarity implements Comparable<PowerRarity> {
    private static volatile PowerRarity defaultRarity = new PowerRarity("Common", ChatColor.WHITE, 1);

    private final String name;
    private final String id;
    private final ChatColor color;
    private final int level;

    public PowerRarity(String name, ChatColor color, int level) {
        this.name = requireNonEmpty(name, "name");
        this.id = normalizeId(name);
        this.color = color == null ? ChatColor.WHITE : color;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public ChatColor getColor() {
        return color;
    }

    public int getLevel() {
        return level;
    }

    public String getColoredName() {
        return color + name;
    }

    @Override
    public int compareTo(PowerRarity other) {
        if (other == null) {
            return 1;
        }
        return Integer.compare(this.level, other.level);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PowerRarity)) {
            return false;
        }
        PowerRarity that = (PowerRarity) other;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PowerRarity{name='" + name + "', color=" + color + ", level=" + level + "}";
    }

    public static PowerRarity getDefault() {
        return defaultRarity;
    }

    public static void setDefault(PowerRarity rarity) {
        if (rarity == null) {
            throw new IllegalArgumentException("rarity cannot be null");
        }
        defaultRarity = rarity;
    }

    private static String normalizeId(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static String requireNonEmpty(String value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " cannot be null");
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(field + " cannot be empty");
        }
        return trimmed;
    }
}
