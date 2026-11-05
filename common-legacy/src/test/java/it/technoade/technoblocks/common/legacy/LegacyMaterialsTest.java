package it.technoade.technoblocks.common.legacy;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LegacyMaterialsTest {

    @Test
    void modernColorName() {
        assertEquals(new MaterialSpec(Material.WOOL, (byte) 14), LegacyMaterials.parse("red_wool"));
    }

    @Test
    void legacyWithData() {
        assertEquals(new MaterialSpec(Material.STAINED_GLASS, (byte) 3), LegacyMaterials.parse("STAINED_GLASS:3"));
    }

    @Test
    void plainName() {
        assertEquals(new MaterialSpec(Material.OBSIDIAN, (byte) 0), LegacyMaterials.parse("OBSIDIAN"));
    }

    @Test
    void woods() {
        assertEquals(new MaterialSpec(Material.LOG_2, (byte) 1), LegacyMaterials.parse("DARK_OAK_LOG"));
        assertEquals(new MaterialSpec(Material.WOOD, (byte) 2), LegacyMaterials.parse("BIRCH_PLANKS"));
    }

    @Test
    void unknownOrBadData() {
        assertNull(LegacyMaterials.parse("NOT_A_BLOCK"));
        assertNull(LegacyMaterials.parse("WOOL:16"));
        assertNull(LegacyMaterials.parse("WOOL:x"));
        assertNull(LegacyMaterials.parse(null));
    }

    @Test
    void concreteMissingOn188() {
        assertNull(LegacyMaterials.parse("RED_CONCRETE"));
    }

    @Test
    void allowed() {
        assertTrue(LegacyMaterials.isAllowed(new MaterialSpec(Material.STONE, (byte) 0)));
        assertTrue(LegacyMaterials.isAllowed(new MaterialSpec(Material.WOOL, (byte) 14)));
    }

    @Test
    void denied() {
        Material[] denied = {Material.SAND, Material.GRAVEL, Material.CHEST, Material.TORCH, Material.CARPET,
                Material.WOODEN_DOOR, Material.BED_BLOCK, Material.SIGN_POST, Material.ANVIL, Material.DIAMOND,
                Material.WOOD_STAIRS, Material.STEP};
        for (Material m : denied) {
            assertFalse(LegacyMaterials.isAllowed(new MaterialSpec(m, (byte) 0)), m.name());
        }
    }
}
