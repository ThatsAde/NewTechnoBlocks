package it.technoade.technoblocks.config;

import it.technoade.technoblocks.api.MaterialSpec;

import java.util.Collections;
import java.util.List;

/**
 * One selectable block from the "blocks" section. Texts are already colored.
 */
public final class BlockEntry {

    private final String key;
    private final int guiSlot;
    private final MaterialSpec material;
    private final String name;
    private final List<String> lore;
    private final int amount;
    private final long durationMillis;
    private final String permission;

    public BlockEntry(String key, int guiSlot, MaterialSpec material, String name, List<String> lore, int amount,
                      long durationMillis, String permission) {
        this.key = key;
        this.guiSlot = guiSlot;
        this.material = material;
        this.name = name;
        this.lore = Collections.unmodifiableList(lore);
        this.amount = amount;
        this.durationMillis = durationMillis;
        this.permission = permission;
    }

    public String getKey() {
        return key;
    }

    public int getGuiSlot() {
        return guiSlot;
    }

    public MaterialSpec getMaterial() {
        return material;
    }

    /** May be null: the item keeps its vanilla name. */
    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public int getAmount() {
        return amount;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    /** Null when the block is free for everyone. */
    public String getPermission() {
        return permission;
    }
}
