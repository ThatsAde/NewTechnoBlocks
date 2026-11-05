package it.technoade.technoblocks.hook;

import it.technoade.technoblocks.data.DataStore;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

/**
 * %technoblocks_total% and %technoblocks_personal%. Only loaded when PlaceholderAPI is installed.
 */
public final class PlaceholderHook extends PlaceholderExpansion {

    private final Plugin plugin;
    private final DataStore data;

    private PlaceholderHook(Plugin plugin, DataStore data) {
        this.plugin = plugin;
        this.data = data;
    }

    public static void registerFor(Plugin plugin, DataStore data) {
        new PlaceholderHook(plugin, data).register();
    }

    @Override
    public String getIdentifier() {
        return "technoblocks";
    }

    @Override
    public String getAuthor() {
        return "TechnoAde";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (params.equalsIgnoreCase("total")) return String.valueOf(data.total());
        if (params.equalsIgnoreCase("personal")) {
            return player == null ? "0" : String.valueOf(data.personal(player.getUniqueId()));
        }
        return null;
    }
}
