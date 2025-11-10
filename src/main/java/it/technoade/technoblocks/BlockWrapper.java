package it.technoade.technoblocks;

import org.bukkit.Bukkit;
import org.bukkit.Material;

public class BlockWrapper {

    public static Material get(String configValue, Material fallback) {
        if (configValue == null || configValue.trim().isEmpty())
            return fallback;

        try {
            Material mat = Material.valueOf(configValue.toUpperCase());
            if (mat != null) return mat;
        } catch (Exception ignored) {}

        try {
            Material mat = Material.matchMaterial(configValue);
            if (mat != null) return mat;
        } catch (Exception ignored) {}

        Bukkit.getLogger().warning("[TechnoBlocks] Material '" + configValue + "' not found, using fallback: " + fallback);
        return fallback;
    }
}
