package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TemporaryBlockRegistryTest {

    private static final MaterialSpec STONE = new MaterialSpec(Material.STONE, (byte) 0);

    @Test
    void uniqueSourceIds() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        TemporaryBlock a = r.add(new BlockPos("hub", 0, 64, 0), STONE, "minecraft:air", 1000, 5000);
        TemporaryBlock b = r.add(new BlockPos("hub", 1, 64, 0), STONE, "minecraft:air", 1000, 5000);
        assertNotEquals(a.getSourceId(), b.getSourceId());
        assertTrue(a.getSourceId() > 1_000_000_000);
        assertTrue(b.getSourceId() > 1_000_000_000);
    }

    @Test
    void samePositionReplaces() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        BlockPos pos = new BlockPos("hub", 0, 64, 0);
        r.add(pos, STONE, "minecraft:air", 1000, 1000);
        TemporaryBlock b = r.add(pos, STONE, "minecraft:air", 1500, 5000);
        assertEquals(1, r.size());
        assertSame(b, r.get(pos));
        assertTrue(r.pollExpired(2500).isEmpty());
        assertEquals(1, r.size());
    }

    @Test
    void pollExpiredOrderedAndRemoves() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        TemporaryBlock three = r.add(new BlockPos("hub", 0, 64, 0), STONE, "s", 0, 3000);
        TemporaryBlock one = r.add(new BlockPos("hub", 1, 64, 0), STONE, "s", 0, 1000);
        TemporaryBlock two = r.add(new BlockPos("hub", 2, 64, 0), STONE, "s", 0, 2000);
        List<TemporaryBlock> expired = r.pollExpired(5000);
        assertEquals(3, expired.size());
        assertSame(one, expired.get(0));
        assertSame(two, expired.get(1));
        assertSame(three, expired.get(2));
        assertEquals(0, r.size());
    }

    @Test
    void notExpiredStays() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        r.add(new BlockPos("hub", 0, 64, 0), STONE, "s", 0, 3000);
        assertTrue(r.pollExpired(2999).isEmpty());
        assertEquals(1, r.size());
    }

    @Test
    void removeThenNoExpiry() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        BlockPos pos = new BlockPos("hub", 0, 64, 0);
        r.add(pos, STONE, "s", 0, 1000);
        assertNotNull(r.remove(pos));
        assertTrue(r.pollExpired(5000).isEmpty());
    }

    @Test
    void drainAllEmpties() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        r.add(new BlockPos("hub", 0, 64, 0), STONE, "s", 0, 3000);
        r.add(new BlockPos("hub", 1, 64, 0), STONE, "s", 0, 3000);
        assertEquals(2, r.drainAll().size());
        assertEquals(0, r.size());
        assertTrue(r.pollExpired(10_000).isEmpty());
    }
}
