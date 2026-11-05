package it.technoade.technoblocks.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * data.yml: placement counters (same format as 2.x) and each player's selected block.
 * Accessed on the main thread; only the file write runs async.
 */
public final class DataStore {

    private final File file;
    private final Map<UUID, Integer> placed = new HashMap<UUID, Integer>();
    private final Map<UUID, String> selected = new HashMap<UUID, String>();
    private int total;

    public DataStore(File file) {
        this.file = file;
    }

    public void load() {
        placed.clear();
        selected.clear();
        total = 0;
        if (!file.exists()) return;
        YamlConfiguration yml = YamlConfiguration.loadConfiguration(file);
        total = yml.getInt("total-placed", 0);
        ConfigurationSection players = yml.getConfigurationSection("players");
        if (players != null) {
            for (String key : players.getKeys(false)) {
                UUID id = parse(key);
                if (id != null) placed.put(id, players.getInt(key));
            }
        }
        ConfigurationSection sel = yml.getConfigurationSection("selected");
        if (sel != null) {
            for (String key : sel.getKeys(false)) {
                UUID id = parse(key);
                if (id != null) selected.put(id, sel.getString(key));
            }
        }
    }

    public int total() {
        return total;
    }

    public int personal(UUID player) {
        Integer count = placed.get(player);
        return count == null ? 0 : count;
    }

    public void increment(UUID player) {
        placed.put(player, personal(player) + 1);
        total++;
    }

    /** @return the selected block key, or null if the player never chose one */
    public String selected(UUID player) {
        return selected.get(player);
    }

    public void select(UUID player, String key) {
        selected.put(player, key);
    }

    public void saveSync() {
        write(snapshot());
    }

    public void saveAsync(Plugin plugin) {
        final String content = snapshot();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                write(content);
            }
        });
    }

    private String snapshot() {
        YamlConfiguration yml = new YamlConfiguration();
        yml.set("total-placed", total);
        for (Map.Entry<UUID, Integer> e : placed.entrySet()) yml.set("players." + e.getKey(), e.getValue());
        for (Map.Entry<UUID, String> e : selected.entrySet()) yml.set("selected." + e.getKey(), e.getValue());
        return yml.saveToString();
    }

    private synchronized void write(String content) {
        try {
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) parent.mkdirs();
            Writer out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            try {
                out.write(content);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            Logger.getLogger("TechnoBlocks").log(Level.WARNING, "Could not save " + file.getName(), e);
        }
    }

    private static UUID parse(String key) {
        try {
            return UUID.fromString(key);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
