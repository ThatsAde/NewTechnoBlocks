package it.technoade.technoblocks.api;

import it.technoade.technoblocks.Main;
import it.technoade.technoblocks.utils.ItemsManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class PlaceholderHook extends PlaceholderExpansion implements Listener {

    private final Main plugin;
    private final HashMap<UUID, Integer> playerPlacedBlocks = new HashMap<>();
    private int totalPlaced = 0;

    private File dataFile;
    private FileConfiguration data;

    public PlaceholderHook(Main plugin) {
        this.plugin = plugin;

        // Load data file
        dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            plugin.saveResource("data.yml", false);
        }
        data = YamlConfiguration.loadConfiguration(dataFile);

        // Load values from file
        totalPlaced = data.getInt("total-placed", 0);
        if (data.contains("players")) {
            data.getConfigurationSection("players").getKeys(false).forEach(uuid ->
                    playerPlacedBlocks.put(UUID.fromString(uuid), data.getInt("players." + uuid))
            );
        }

        // Register listener una sola volta
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @Override
    @NotNull
    public String getIdentifier() {
        return "technoblocks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "ThatsAde";
    }

    @Override
    public @NotNull String getVersion() {
        return "1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    private final HashSet<UUID> recentlyCounted = new HashSet<>();

    @EventHandler
    public void blockListener(final BlockPlaceEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        // Salta eventi cancellati o blocchi vietati
        if (e.isCancelled()) return;
        if (e.getBlockPlaced().getType() == Material.CHEST) return;
        if (!ItemsManager.item.containsKey(e.getItemInHand())) return;

        // Controllo per non contare due volte
        if (recentlyCounted.contains(uuid)) return;
        recentlyCounted.add(uuid);

        int newCount = playerPlacedBlocks.getOrDefault(uuid, 0) + 1;
        playerPlacedBlocks.put(uuid, newCount);

        totalPlaced++;
        saveData();

        // Rimuovo il flag dopo pochi tick
        Bukkit.getScheduler().runTaskLater(plugin, () -> recentlyCounted.remove(uuid), 1L);
    }


    private void saveData() {
        try {
            data.set("total-placed", totalPlaced);
            playerPlacedBlocks.forEach((uuid, count) -> data.set("players." + uuid, count));
            data.save(dataFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        if (params.equalsIgnoreCase("total")) {
            return String.valueOf(totalPlaced);
        }

        if (params.equalsIgnoreCase("personal")) {
            return String.valueOf(playerPlacedBlocks.getOrDefault(player.getUniqueId(), 0));
        }

        return null;
    }
}
