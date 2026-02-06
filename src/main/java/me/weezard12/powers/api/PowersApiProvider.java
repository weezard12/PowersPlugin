package me.weezard12.powers.api;

import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicesManager;

public final class PowersApiProvider {
    private PowersApiProvider() {
    }

    public static PowersApi get() {
        ServicesManager services = Bukkit.getServicesManager();
        return services.load(PowersApi.class);
    }
}
