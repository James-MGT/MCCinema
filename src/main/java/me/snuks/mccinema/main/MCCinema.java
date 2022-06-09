package me.snuks.mccinema.main;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class MCCinema extends JavaPlugin {

    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("createimage").setExecutor(new CreateImage(this));
        getLogger().info("MCCinema has been enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("MCCinema has been disabled.");
    }
}
