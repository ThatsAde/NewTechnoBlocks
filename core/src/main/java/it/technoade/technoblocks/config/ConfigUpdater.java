package it.technoade.technoblocks.config;

import org.bukkit.configuration.ConfigurationSection;

/**
 * Adds keys introduced by newer versions without touching the admin's values.
 */
public final class ConfigUpdater {

    public static final int CURRENT_VERSION = 3;

    /** Excluded from merging so deleted example blocks don't come back. */
    private static final String BLOCKS = "blocks";

    private ConfigUpdater() {
    }

    /** @return true if cfg was changed */
    public static boolean merge(ConfigurationSection cfg, ConfigurationSection defaults) {
        boolean changed = false;
        for (String key : defaults.getKeys(true)) {
            if (key.equals(BLOCKS) || key.startsWith(BLOCKS + ".")) continue;
            if (defaults.isConfigurationSection(key)) continue;
            if (!cfg.contains(key)) {
                cfg.set(key, defaults.get(key));
                changed = true;
            }
        }
        return changed;
    }

    /** True for configs written by TechnoBlocks 2.x. */
    public static boolean isOldFormat(ConfigurationSection cfg) {
        if (cfg.contains("itemselector")) return true;
        return cfg.contains("config-version") && cfg.getInt("config-version") < CURRENT_VERSION;
    }
}
