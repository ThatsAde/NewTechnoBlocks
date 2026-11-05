package it.technoade.technoblocks.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable snapshot of config.yml. Rebuilt on every reload.
 */
public final class PluginSettings {

    private final WorldFilter worlds;
    private final SelectorSettings selector;
    private final int placedSlot;
    private final boolean cracksEnabled;
    private final int viewDistance;
    private final Map<String, BlockEntry> blocks;

    public PluginSettings(WorldFilter worlds, SelectorSettings selector, int placedSlot, boolean cracksEnabled,
                          int viewDistance, LinkedHashMap<String, BlockEntry> blocks) {
        this.worlds = worlds;
        this.selector = selector;
        this.placedSlot = placedSlot;
        this.cracksEnabled = cracksEnabled;
        this.viewDistance = viewDistance;
        this.blocks = Collections.unmodifiableMap(new LinkedHashMap<String, BlockEntry>(blocks));
    }

    public WorldFilter getWorlds() {
        return worlds;
    }

    public SelectorSettings getSelector() {
        return selector;
    }

    public int getPlacedSlot() {
        return placedSlot;
    }

    public boolean isCracksEnabled() {
        return cracksEnabled;
    }

    public int getViewDistance() {
        return viewDistance;
    }

    /** In config order. */
    public Map<String, BlockEntry> getBlocks() {
        return blocks;
    }

    /** The first block in config order, or null when there are none. */
    public BlockEntry defaultBlock() {
        return blocks.isEmpty() ? null : blocks.values().iterator().next();
    }
}
