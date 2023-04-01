package it.technoade.technoblocks;

import it.technoade.technoblocks.tasks.Events;
import it.technoade.technoblocks.utils.Gui;
import it.technoade.technoblocks.utils.ItemsManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    public static Main plugin;

    @Override
    public void onEnable() {

        plugin = this;
        startUpTasks(plugin);
        startUpMessages();

    }

    @Override
    public void onDisable() {
        Events.saveStack.forEach(block -> {
            block.setType(Material.AIR);
        });
        reloadConfig();
    }

    public static void startUpTasks(Main plugin) {
        plugin.saveDefaultConfig();
        ItemsManager.registerItems();
        plugin.getServer().getPluginManager().registerEvents(new Events(), plugin);
        plugin.getCommand("technoblocks").setExecutor(new Commands());
    }

    public static void startUpMessages() {
        Bukkit.getLogger().info("§8<§7--------------------------------------------§8>");
        Bukkit.getLogger().info("        §bTechnoBlocks §7started succefully");
        Bukkit.getLogger().info("§8<§7--------------------------------------------§8>");
    }
}
