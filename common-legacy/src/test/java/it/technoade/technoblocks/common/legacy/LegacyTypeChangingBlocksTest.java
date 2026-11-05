package it.technoade.technoblocks.common.legacy;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/** Blocks that change type during their lifetime would never be restored, so they are denied. */
class LegacyTypeChangingBlocksTest {

    @Test
    void denied() {
        Material[] denied = {Material.GRASS, Material.DIRT, Material.MYCEL, Material.SOIL, Material.REDSTONE_ORE,
                Material.GLOWING_REDSTONE_ORE, Material.DRAGON_EGG, Material.PUMPKIN};
        for (Material m : denied) {
            assertFalse(LegacyMaterials.isAllowed(new MaterialSpec(m, (byte) 0)), m.name());
        }
    }
}
