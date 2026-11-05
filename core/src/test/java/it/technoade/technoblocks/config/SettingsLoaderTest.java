package it.technoade.technoblocks.config;

import it.technoade.technoblocks.FakeAdapter;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class SettingsLoaderTest {

    private static final Logger LOG = Logger.getLogger("test");

    private static PluginSettings load(String... blockLines) {
        ArrayList<String> lines = new ArrayList<String>(Arrays.asList(
                "worlds:", "  mode: whitelist", "  list: [hub]",
                "selector:", "  slot: 8", "  material: CHEST", "  name: '&bSelector'",
                "  gui:", "    title: '&bTitle'", "    rows: 3",
                "placed-blocks:", "  slot: 4", "  default-duration: 5",
                "cracks:", "  enabled: true", "  view-distance: 32"));
        lines.addAll(Arrays.asList(blockLines));
        YamlConfiguration cfg = Yaml.of(lines.toArray(new String[0]));
        return SettingsLoader.load(cfg, new FakeAdapter(), LOG);
    }

    @Test
    void perBlockDurationAndDefault() {
        PluginSettings s = load("blocks:",
                "  obsidian:", "    gui-slot: 10", "    material: OBSIDIAN", "    duration: 10",
                "  stone:", "    gui-slot: 12", "    material: STONE");
        assertEquals(10_000L, s.getBlocks().get("obsidian").getDurationMillis());
        assertEquals(5_000L, s.getBlocks().get("stone").getDurationMillis());
    }

    @Test
    void invalidDurationFallsBack() {
        PluginSettings s = load("blocks:",
                "  a:", "    gui-slot: 10", "    material: STONE", "    duration: 0",
                "  b:", "    gui-slot: 11", "    material: STONE", "    duration: -3");
        assertEquals(5_000L, s.getBlocks().get("a").getDurationMillis());
        assertEquals(5_000L, s.getBlocks().get("b").getDurationMillis());
    }

    @Test
    void slotOutOfRowsSkipped() {
        PluginSettings s = load("blocks:", "  a:", "    gui-slot: 27", "    material: STONE");
        assertFalse(s.getBlocks().containsKey("a"));
    }

    @Test
    void duplicateSlotSkipsSecond() {
        PluginSettings s = load("blocks:",
                "  first:", "    gui-slot: 10", "    material: STONE",
                "  second:", "    gui-slot: 10", "    material: OBSIDIAN");
        assertTrue(s.getBlocks().containsKey("first"));
        assertFalse(s.getBlocks().containsKey("second"));
    }

    @Test
    void unknownAndDisallowedMaterialSkipped() {
        PluginSettings s = load("blocks:",
                "  fake:", "    gui-slot: 10", "    material: NOT_REAL",
                "  sand:", "    gui-slot: 11", "    material: SAND",
                "  ok:", "    gui-slot: 12", "    material: STONE");
        assertEquals(Arrays.asList("ok"), new ArrayList<String>(s.getBlocks().keySet()));
    }

    @Test
    void emptyBlocksGivesNullDefault() {
        PluginSettings s = load("blocks: {}");
        assertTrue(s.getBlocks().isEmpty());
        assertNull(s.defaultBlock());
    }

    @Test
    void selectorAndPlacedSlotCollision() {
        YamlConfiguration cfg = Yaml.of("selector:", "  slot: 4", "  material: CHEST",
                "placed-blocks:", "  slot: 4", "blocks: {}");
        PluginSettings s = SettingsLoader.load(cfg, new FakeAdapter(), LOG);
        assertEquals(4, s.getSelector().getSlot());
        assertNotEquals(4, s.getPlacedSlot());
    }

    @Test
    void colorsTranslated() {
        PluginSettings s = load("blocks:", "  a:", "    gui-slot: 10", "    material: OBSIDIAN",
                "    name: '&4Obsidian'", "    lore: ['&7Nice']");
        assertEquals("§4Obsidian", s.getBlocks().get("a").getName());
        assertEquals(Arrays.asList("§7Nice"), s.getBlocks().get("a").getLore());
        assertEquals("§bTitle", s.getSelector().getGuiTitle());
    }

    @Test
    void firstBlockIsDefault() {
        PluginSettings s = load("blocks:",
                "  zeta:", "    gui-slot: 14", "    material: STONE",
                "  alpha:", "    gui-slot: 10", "    material: OBSIDIAN");
        assertEquals("zeta", s.defaultBlock().getKey());
        assertEquals(Material.OBSIDIAN, s.getBlocks().get("alpha").getMaterial().getMaterial());
    }

    @Test
    void amountAndViewDistanceClamped() {
        YamlConfiguration cfg = Yaml.of("cracks:", "  view-distance: 0",
                "blocks:", "  a:", "    gui-slot: 1", "    material: STONE", "    amount: 99");
        PluginSettings s = SettingsLoader.load(cfg, new FakeAdapter(), LOG);
        assertEquals(1, s.getBlocks().get("a").getAmount());
        assertEquals(32, s.getViewDistance());
    }
}
