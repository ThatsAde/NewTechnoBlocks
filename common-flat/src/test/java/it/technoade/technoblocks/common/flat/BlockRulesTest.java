package it.technoade.technoblocks.common.flat;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlockRulesTest {

    @Test
    void allowed() {
        Material[] allowed = {Material.STONE, Material.OBSIDIAN, Material.RED_WOOL, Material.GLASS, Material.OAK_PLANKS};
        for (Material m : allowed) {
            assertTrue(BlockRules.isAllowed(m), m.name());
        }
    }

    @Test
    void denied() {
        Material[] denied = {Material.SAND, Material.RED_CONCRETE_POWDER, Material.CHEST, Material.BARREL,
                Material.TORCH, Material.WHITE_CARPET, Material.OAK_DOOR, Material.RED_BED, Material.OAK_SIGN,
                Material.ANVIL, Material.DIAMOND, Material.OAK_STAIRS, Material.STONE_SLAB, Material.SHULKER_BOX,
                Material.RED_SHULKER_BOX, Material.TALL_GRASS, Material.ICE, Material.SPONGE};
        for (Material m : denied) {
            assertFalse(BlockRules.isAllowed(m), m.name());
        }
    }
}
