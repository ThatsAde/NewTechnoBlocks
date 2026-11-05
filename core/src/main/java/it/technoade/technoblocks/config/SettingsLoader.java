package it.technoade.technoblocks.config;

import it.technoade.technoblocks.api.MaterialSpec;
import it.technoade.technoblocks.api.VersionAdapter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Turns config.yml into {@link PluginSettings}, fixing bad values with a warning.
 */
public final class SettingsLoader {

    private static final int DEFAULT_SELECTOR_SLOT = 8;
    private static final int DEFAULT_PLACED_SLOT = 4;
    private static final int DEFAULT_DURATION = 5;
    private static final int DEFAULT_ROWS = 3;
    private static final int DEFAULT_VIEW_DISTANCE = 32;

    private SettingsLoader() {
    }

    public static PluginSettings load(ConfigurationSection root, VersionAdapter adapter, Logger log) {
        WorldFilter worlds = loadWorlds(root);
        SelectorSettings selector = loadSelector(root, adapter, log);

        int placedSlot = hotbarSlot(root, "placed-blocks.slot", DEFAULT_PLACED_SLOT, log);
        if (placedSlot == selector.getSlot()) {
            int free = DEFAULT_PLACED_SLOT;
            for (int i = 0; i < 9; i++) {
                free = (DEFAULT_PLACED_SLOT + i) % 9;
                if (free != selector.getSlot()) break;
            }
            log.warning("placed-blocks.slot is the same as selector.slot (" + placedSlot + "), using slot " + free);
            placedSlot = free;
        }

        int defaultDuration = root.getInt("placed-blocks.default-duration", DEFAULT_DURATION);
        if (defaultDuration <= 0) {
            log.warning("placed-blocks.default-duration must be positive, using " + DEFAULT_DURATION);
            defaultDuration = DEFAULT_DURATION;
        }

        boolean cracks = root.getBoolean("cracks.enabled", true);
        int viewDistance = root.getInt("cracks.view-distance", DEFAULT_VIEW_DISTANCE);
        if (viewDistance < 1) {
            log.warning("cracks.view-distance must be at least 1, using " + DEFAULT_VIEW_DISTANCE);
            viewDistance = DEFAULT_VIEW_DISTANCE;
        }

        LinkedHashMap<String, BlockEntry> blocks = loadBlocks(root, adapter, log, selector.getGuiRows(), defaultDuration);
        return new PluginSettings(worlds, selector, placedSlot, cracks, viewDistance, blocks);
    }

    private static WorldFilter loadWorlds(ConfigurationSection root) {
        if (!root.isConfigurationSection("worlds")) {
            return new WorldFilter(false, new HashSet<String>());
        }
        boolean whitelist = !"blacklist".equalsIgnoreCase(root.getString("worlds.mode", "whitelist"));
        return new WorldFilter(whitelist, new HashSet<String>(root.getStringList("worlds.list")));
    }

    private static SelectorSettings loadSelector(ConfigurationSection root, VersionAdapter adapter, Logger log) {
        int slot = hotbarSlot(root, "selector.slot", DEFAULT_SELECTOR_SLOT, log);
        String materialName = root.getString("selector.material", "CHEST");
        MaterialSpec material = adapter.parseMaterial(materialName);
        if (material == null) {
            log.warning("selector.material '" + materialName + "' is not a valid material here, using CHEST");
            material = new MaterialSpec(Material.CHEST, (byte) 0);
        }
        int rows = root.getInt("selector.gui.rows", DEFAULT_ROWS);
        if (rows < 1 || rows > 6) {
            log.warning("selector.gui.rows must be between 1 and 6, using " + DEFAULT_ROWS);
            rows = DEFAULT_ROWS;
        }
        return new SelectorSettings(slot, material,
                color(root.getString("selector.name", "&bBlock Selector")),
                colorAll(root.getStringList("selector.lore")),
                color(root.getString("selector.gui.title", "&bSelector")),
                rows);
    }

    private static LinkedHashMap<String, BlockEntry> loadBlocks(ConfigurationSection root, VersionAdapter adapter,
                                                                Logger log, int rows, int defaultDuration) {
        LinkedHashMap<String, BlockEntry> blocks = new LinkedHashMap<String, BlockEntry>();
        ConfigurationSection section = root.getConfigurationSection("blocks");
        if (section == null) return blocks;

        Set<Integer> usedSlots = new HashSet<Integer>();
        for (String key : section.getKeys(false)) {
            ConfigurationSection b = section.getConfigurationSection(key);
            if (b == null) {
                log.warning("blocks." + key + " is not a section, skipped");
                continue;
            }
            int guiSlot = b.getInt("gui-slot", -1);
            if (guiSlot < 0 || guiSlot >= rows * 9) {
                log.warning("blocks." + key + ".gui-slot " + guiSlot + " is outside the GUI (0-" + (rows * 9 - 1)
                        + "), block skipped");
                continue;
            }
            if (!usedSlots.add(guiSlot)) {
                log.warning("blocks." + key + ".gui-slot " + guiSlot + " is already used, block skipped");
                continue;
            }
            String materialName = b.getString("material");
            MaterialSpec material = adapter.parseMaterial(materialName);
            if (material == null) {
                log.warning("blocks." + key + ".material '" + materialName + "' is not a valid material here, block skipped");
                usedSlots.remove(guiSlot);
                continue;
            }
            if (!adapter.isAllowedBlock(material)) {
                log.warning("blocks." + key + ".material '" + materialName + "' is not a simple solid block, block skipped");
                usedSlots.remove(guiSlot);
                continue;
            }
            int amount = b.getInt("amount", 1);
            if (amount < 1 || amount > 64) {
                log.warning("blocks." + key + ".amount must be between 1 and 64, using 1");
                amount = 1;
            }
            int duration = b.getInt("duration", defaultDuration);
            if (duration <= 0) {
                log.warning("blocks." + key + ".duration must be positive, using " + defaultDuration);
                duration = defaultDuration;
            }
            String permission = b.getString("permission");
            if (permission != null && permission.trim().isEmpty()) permission = null;
            String name = b.getString("name");
            blocks.put(key, new BlockEntry(key, guiSlot, material, name == null ? null : color(name),
                    colorAll(b.getStringList("lore")), amount, duration * 1000L, permission));
        }
        return blocks;
    }

    private static int hotbarSlot(ConfigurationSection root, String path, int fallback, Logger log) {
        int slot = root.getInt(path, fallback);
        if (slot < 0 || slot > 8) {
            log.warning(path + " must be between 0 and 8, using " + fallback);
            return fallback;
        }
        return slot;
    }

    static String color(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    static List<String> colorAll(List<String> lines) {
        List<String> out = new ArrayList<String>(lines.size());
        for (String line : lines) out.add(color(line));
        return out;
    }
}
