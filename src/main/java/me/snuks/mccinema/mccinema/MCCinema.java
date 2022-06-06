package me.snuks.mccinema.mccinema;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCCinema extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("MCCinema has been enabled.");
        Bukkit.getPluginCommand("createscreen").setExecutor(new CreateScreen(this));
    }

    @Override
    public void onDisable() {
        getLogger().info("MCCinema has been disabled.");
    }
}
