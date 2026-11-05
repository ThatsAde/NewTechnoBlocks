package it.technoade.technoblocks.adapter;

import it.technoade.technoblocks.api.VersionAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.logging.Level;

/**
 * Instantiates the adapter for the running server.
 */
public final class AdapterLoader {

    private AdapterLoader() {
    }

    /** @return the adapter, or null (already logged) if the server is not supported */
    public static VersionAdapter load(Plugin plugin) {
        String craftPackage = Bukkit.getServer().getClass().getPackage().getName();
        String className = AdapterResolver.resolve(hasSourceIdDamageApi(), craftPackage);
        if (className == null) {
            plugin.getLogger().severe("Unsupported server version: " + Bukkit.getVersion() + " (" + craftPackage + ")");
            return null;
        }
        try {
            return (VersionAdapter) Class.forName(className).getConstructor(Plugin.class).newInstance(plugin);
        } catch (Throwable t) {
            plugin.getLogger().log(Level.SEVERE, "Could not load adapter " + className + " for "
                    + Bukkit.getVersion() + " (" + craftPackage + ")", t);
            return null;
        }
    }

    private static boolean hasSourceIdDamageApi() {
        try {
            Player.class.getMethod("sendBlockDamage", Location.class, float.class, int.class);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
