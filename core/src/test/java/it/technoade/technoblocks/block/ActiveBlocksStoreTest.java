package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ActiveBlocksStoreTest {

    @TempDir
    File dir;

    @Test
    void encodeFormat() {
        TemporaryBlockRegistry r = new TemporaryBlockRegistry();
        TemporaryBlock b = r.add(new BlockPos("hub", 1, 64, -2), new MaterialSpec(Material.OBSIDIAN, (byte) 0),
                "minecraft:air", 0, 1000);
        assertEquals("hub;1;64;-2|OBSIDIAN|minecraft:air", ActiveBlocksStore.encode(b));
        TemporaryBlock wool = r.add(new BlockPos("hub", 2, 64, -2), new MaterialSpec(Material.WOOL, (byte) 14),
                "AIR:0", 0, 1000);
        assertEquals("hub;2;64;-2|WOOL:14|AIR:0", ActiveBlocksStore.encode(wool));
    }

    @Test
    void decodeRoundTrip() {
        ActiveBlocksStore.Entry e = ActiveBlocksStore.decode("hub;1;64;-2|OBSIDIAN|minecraft:water[level=0]");
        assertEquals(new BlockPos("hub", 1, 64, -2), e.getPos());
        assertEquals("OBSIDIAN", e.getPlaced());
        assertEquals("minecraft:water[level=0]", e.getReplacedState());
        assertNull(ActiveBlocksStore.decode("garbage"));
        assertNull(ActiveBlocksStore.decode("hub;x;64;0|STONE|AIR:0"));
    }

    @Test
    void writeThenReadRoundTrip() {
        ActiveBlocksStore store = new ActiveBlocksStore(new File(dir, "active-blocks.yml"));
        List<String> lines = Arrays.asList("hub;1;64;-2|OBSIDIAN|minecraft:air", "hub;2;64;-2|WOOL:14|AIR:0");
        store.writeLinesSync(lines);
        assertEquals(lines, store.readLines());
    }

    @Test
    void readMissingFileIsEmpty() {
        assertTrue(new ActiveBlocksStore(new File(dir, "nope.yml")).readLines().isEmpty());
    }

    @Test
    void unknownWorldKept() {
        List<String> lines = Arrays.asList(
                "hub;1;64;-2|OBSIDIAN|minecraft:air",
                "lobby2;5;70;5|STONE|minecraft:air",
                "broken line");
        List<String> kept = ActiveBlocksStore.linesToKeep(lines, w -> w.equals("hub"));
        assertEquals(Arrays.asList("lobby2;5;70;5|STONE|minecraft:air"), kept);
    }
}
