package it.technoade.technoblocks.common.flat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Blocks that change type during their lifetime would never be restored, so they are denied. */
class TypeChangingBlocksTest {

    @Test
    void deniedMaterials() {
        Material[] denied = {Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT, Material.PODZOL,
                Material.MYCELIUM, Material.FARMLAND, Material.GRASS_PATH, Material.OAK_LOG, Material.STRIPPED_OAK_LOG,
                Material.OAK_WOOD, Material.PUMPKIN, Material.BRAIN_CORAL_BLOCK, Material.DEAD_BRAIN_CORAL_BLOCK,
                Material.DRAGON_EGG};
        for (Material m : denied) {
            assertFalse(BlockRules.isAllowed(m), m.name());
        }
    }

    @Test
    void deniedNamesNewerThanCompileApi() {
        String[] denied = {"COPPER_BLOCK", "WAXED_CUT_COPPER", "EXPOSED_COPPER", "MOSS_BLOCK", "CRIMSON_NYLIUM",
                "SCULK", "SCULK_CATALYST", "BUDDING_AMETHYST", "ROOTED_DIRT", "DIRT_PATH", "CRIMSON_STEM",
                "WARPED_HYPHAE", "BAMBOO_BLOCK", "CHERRY_LOG"};
        for (String name : denied) {
            assertTrue(BlockRules.isDeniedName(name), name);
        }
    }

    @Test
    void stableBlocksStillAllowed() {
        assertFalse(BlockRules.isDeniedName("STONE"));
        assertFalse(BlockRules.isDeniedName("OAK_PLANKS"));
        assertFalse(BlockRules.isDeniedName("WHITE_CONCRETE"));
    }
}
