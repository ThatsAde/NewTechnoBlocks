package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Blocks of a world being unloaded must be taken out so they can be restored while the world still exists. */
class WorldUnloadTest {

    private static final MaterialSpec STONE = new MaterialSpec(Material.STONE, (byte) 0);

    @Test
    void drainWorldTakesOnlyThatWorld() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        r.add(new BlockPos("hub", 0, 64, 0), STONE, "s", 0, 5000);
        r.add(new BlockPos("arena", 0, 64, 0), STONE, "s", 0, 5000);
        r.add(new BlockPos("arena", 1, 64, 0), STONE, "s", 0, 5000);
        List<TemporaryBlock> drained = r.drainWorld("arena");
        assertEquals(2, drained.size());
        assertEquals(1, r.size());
        assertTrue(r.pollExpired(10_000).size() == 1);
    }
}
