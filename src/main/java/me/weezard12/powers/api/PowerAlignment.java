package me.weezard12.powers.api;

import java.util.Locale;

public enum PowerAlignment {
    NEUTRAL,
    LIGHT,
    DARK;

    public String getDisplayName() {
        String lower = name().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }
}
