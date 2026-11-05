package it.technoade.technoblocks.config;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Worlds where the plugin is active.
 */
public final class WorldFilter {

    private final boolean whitelist;
    private final Set<String> worlds = new HashSet<String>();

    public WorldFilter(boolean whitelist, Set<String> worlds) {
        this.whitelist = whitelist;
        for (String world : worlds) this.worlds.add(world.toLowerCase(Locale.ROOT));
    }

    public boolean isEnabled(String worldName) {
        return worlds.contains(worldName.toLowerCase(Locale.ROOT)) == whitelist;
    }
}
