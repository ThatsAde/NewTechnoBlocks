package it.technoade.technoblocks.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigUpdaterTest {

    private static YamlConfiguration defaults() {
        return Yaml.of("config-version: 3", "cracks:", "  enabled: true", "  view-distance: 32",
                "blocks:", "  obsidian:", "    material: OBSIDIAN");
    }

    @Test
    void addsMissingKeys() {
        YamlConfiguration cfg = Yaml.of("config-version: 3", "blocks:", "  mine:", "    material: STONE");
        assertTrue(ConfigUpdater.merge(cfg, defaults()));
        assertEquals(32, cfg.getInt("cracks.view-distance"));
    }

    @Test
    void doesNotRestoreDeletedExampleBlocks() {
        YamlConfiguration cfg = Yaml.of("config-version: 3", "blocks:", "  mine:", "    material: STONE");
        ConfigUpdater.merge(cfg, defaults());
        assertFalse(cfg.contains("blocks.obsidian"));
        assertTrue(cfg.contains("blocks.mine"));
    }

    @Test
    void keepsUserValues() {
        YamlConfiguration cfg = Yaml.of("config-version: 3", "cracks:", "  enabled: true", "  view-distance: 10",
                "blocks: {}");
        ConfigUpdater.merge(cfg, defaults());
        assertEquals(10, cfg.getInt("cracks.view-distance"));
    }

    @Test
    void noChangeReturnsFalse() {
        YamlConfiguration cfg = Yaml.of("config-version: 3", "cracks:", "  enabled: false", "  view-distance: 20",
                "blocks:", "  mine:", "    material: STONE");
        assertFalse(ConfigUpdater.merge(cfg, defaults()));
    }

    @Test
    void detectsOldFormat() {
        assertTrue(ConfigUpdater.isOldFormat(Yaml.of("config-version: 1", "itemselector:", "  position: 8")));
        assertTrue(ConfigUpdater.isOldFormat(Yaml.of("itemselector:", "  position: 8")));
        assertFalse(ConfigUpdater.isOldFormat(Yaml.of("config-version: 3")));
        assertFalse(ConfigUpdater.isOldFormat(Yaml.of("")));
    }
}
