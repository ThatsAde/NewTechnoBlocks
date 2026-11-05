package it.technoade.technoblocks.common.legacy;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Material names on 1.8-1.12: accepts legacy "NAME:data" and the common 1.13+ names.
 */
public final class LegacyMaterials {

    private static final String[] COLORS = {"WHITE", "ORANGE", "MAGENTA", "LIGHT_BLUE", "YELLOW", "LIME", "PINK",
            "GRAY", "LIGHT_GRAY", "CYAN", "PURPLE", "BLUE", "BROWN", "GREEN", "RED", "BLACK"};
    private static final String[] WOODS = {"OAK", "SPRUCE", "BIRCH", "JUNGLE", "ACACIA", "DARK_OAK"};

    /** modern name -> "LEGACY_NAME:data" */
    private static final Map<String, String> MODERN_TO_LEGACY = new HashMap<String, String>();

    private static final Set<String> DENIED = new HashSet<String>(Arrays.asList(
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "FURNACE", "BURNING_FURNACE", "DISPENSER", "DROPPER", "HOPPER",
            "BEACON", "BREWING_STAND", "JUKEBOX", "MOB_SPAWNER", "ENCHANTMENT_TABLE", "ANVIL", "TNT", "CAKE_BLOCK",
            "BED_BLOCK", "SIGN_POST", "WALL_SIGN", "STANDING_BANNER", "WALL_BANNER", "PISTON_BASE",
            "PISTON_STICKY_BASE", "PISTON_EXTENSION", "PISTON_MOVING_PIECE", "COMMAND", "NOTE_BLOCK",
            "DAYLIGHT_DETECTOR", "DAYLIGHT_DETECTOR_INVERTED", "CAULDRON", "FLOWER_POT", "SKULL", "IRON_DOOR_BLOCK",
            "WOODEN_DOOR", "ICE", "FROSTED_ICE", "SPONGE",
            // change type while placed (tilled, trampled, spread, lit, teleported, carved) and would never be restored
            "GRASS", "DIRT", "MYCEL", "SOIL", "GRASS_PATH", "REDSTONE_ORE", "GLOWING_REDSTONE_ORE", "DRAGON_EGG",
            "PUMPKIN"));

    static {
        for (int i = 0; i < COLORS.length; i++) {
            MODERN_TO_LEGACY.put(COLORS[i] + "_WOOL", "WOOL:" + i);
            MODERN_TO_LEGACY.put(COLORS[i] + "_STAINED_GLASS", "STAINED_GLASS:" + i);
            MODERN_TO_LEGACY.put(COLORS[i] + "_TERRACOTTA", "STAINED_CLAY:" + i);
            MODERN_TO_LEGACY.put(COLORS[i] + "_CONCRETE", "CONCRETE:" + i);
        }
        for (int i = 0; i < WOODS.length; i++) {
            MODERN_TO_LEGACY.put(WOODS[i] + "_PLANKS", "WOOD:" + i);
            MODERN_TO_LEGACY.put(WOODS[i] + "_LOG", i < 4 ? "LOG:" + i : "LOG_2:" + (i - 4));
        }
        MODERN_TO_LEGACY.put("TERRACOTTA", "HARD_CLAY:0");
        MODERN_TO_LEGACY.put("GRANITE", "STONE:1");
        MODERN_TO_LEGACY.put("POLISHED_GRANITE", "STONE:2");
        MODERN_TO_LEGACY.put("DIORITE", "STONE:3");
        MODERN_TO_LEGACY.put("POLISHED_DIORITE", "STONE:4");
        MODERN_TO_LEGACY.put("ANDESITE", "STONE:5");
        MODERN_TO_LEGACY.put("POLISHED_ANDESITE", "STONE:6");
        MODERN_TO_LEGACY.put("STONE_BRICKS", "SMOOTH_BRICK:0");
        MODERN_TO_LEGACY.put("BRICKS", "BRICK:0");
        MODERN_TO_LEGACY.put("END_STONE", "ENDER_STONE:0");
    }

    private LegacyMaterials() {
    }

    /** @return the spec, or null if the name or data value is unknown on this server */
    public static MaterialSpec parse(String spec) {
        if (spec == null) return null;
        String name = spec.trim().toUpperCase(Locale.ROOT);
        if (name.isEmpty()) return null;
        if (name.indexOf(':') < 0 && MODERN_TO_LEGACY.containsKey(name)) {
            name = MODERN_TO_LEGACY.get(name);
        }
        byte data = 0;
        int colon = name.indexOf(':');
        if (colon >= 0) {
            int value;
            try {
                value = Integer.parseInt(name.substring(colon + 1));
            } catch (NumberFormatException e) {
                return null;
            }
            if (value < 0 || value > 15) return null;
            data = (byte) value;
            name = name.substring(0, colon);
        }
        Material material = Material.getMaterial(name);
        return material == null ? null : new MaterialSpec(material, data);
    }

    public static boolean isAllowed(MaterialSpec spec) {
        Material m = spec.getMaterial();
        if (!m.isBlock() || !m.isSolid() || m.hasGravity()) return false;
        String name = m.name();
        return !DENIED.contains(name)
                && !name.endsWith("_DOOR")
                && !name.endsWith("_SHULKER_BOX")
                && !name.endsWith("FENCE_GATE")
                && !name.endsWith("_PLATE")
                && !name.endsWith("STAIRS")
                && !name.contains("SLAB")
                && !name.endsWith("STEP");
    }
}
