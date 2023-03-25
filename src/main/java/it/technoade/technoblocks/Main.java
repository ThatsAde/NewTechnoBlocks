package it.technoade.technoblocks;

import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private static Main plugin;

    @Override
    public void onEnable() {

        plugin = this;

    }

    public static Main getPlugin() {
        return plugin;
    }
}
