package it.technoade.technoblocks.common.flat;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Which 1.13+ materials may be used as temporary blocks. Names are compared as
 * strings so materials newer than the compile API are covered too.
 */
public final class BlockRules {

    private static final Set<String> DENIED = new HashSet<String>(Arrays.asList(
            "CHEST", "TRAPPED_CHEST", "ENDER_CHEST", "BARREL", "FURNACE", "BLAST_FURNACE", "SMOKER", "DISPENSER",
            "DROPPER", "HOPPER", "BEACON", "BREWING_STAND", "JUKEBOX", "SPAWNER", "ENCHANTING_TABLE", "TNT", "CAKE",
            "PISTON", "STICKY_PISTON", "PISTON_HEAD", "MOVING_PISTON", "COMMAND_BLOCK", "CHAIN_COMMAND_BLOCK",
            "REPEATING_COMMAND_BLOCK", "NOTE_BLOCK", "DAYLIGHT_DETECTOR", "CAULDRON", "FLOWER_POT", "LECTERN", "BELL",
            "CAMPFIRE", "SOUL_CAMPFIRE", "CONDUIT", "RESPAWN_ANCHOR", "LODESTONE", "BEEHIVE", "BEE_NEST", "CRAFTER",
            "DECORATED_POT", "CHISELED_BOOKSHELF", "VAULT", "TRIAL_SPAWNER", "ICE", "FROSTED_ICE", "SPONGE",
            "WET_SPONGE",
            // change type while placed (tilled, stripped, spread, carved, teleported) and would never be restored
            "GRASS_BLOCK", "DIRT", "COARSE_DIRT", "ROOTED_DIRT", "PODZOL", "MYCELIUM", "FARMLAND", "GRASS_PATH",
            "DIRT_PATH", "MOSS_BLOCK", "PUMPKIN", "DRAGON_EGG", "BUDDING_AMETHYST", "BAMBOO_BLOCK"));

    private static final String[] DENIED_SUFFIXES = {"_DOOR", "_BED", "_SIGN", "_BANNER", "SHULKER_BOX",
            "_FENCE_GATE", "_PRESSURE_PLATE", "_STAIRS", "_SLAB", "_TRAPDOOR", "_HEAD", "_SKULL", "ANVIL",
            "_CANDLE_CAKE", "_COPPER_BULB", "CHEST", "_LOG", "_WOOD", "_STEM", "_HYPHAE", "_NYLIUM"};

    /** Families that oxidise, die or spread. */
    private static final String[] DENIED_PARTS = {"COPPER", "CORAL", "SCULK"};

    private BlockRules() {
    }

    public static boolean isAllowed(Material m) {
        if (!m.isBlock() || !m.isSolid() || m.hasGravity()) return false;
        return !isDeniedName(m.name());
    }

    static boolean isDeniedName(String name) {
        if (DENIED.contains(name)) return true;
        for (String suffix : DENIED_SUFFIXES) {
            if (name.endsWith(suffix)) return true;
        }
        for (String part : DENIED_PARTS) {
            if (name.contains(part)) return true;
        }
        return false;
    }
}
