package it.technoade.technoblocks.config;

import it.technoade.technoblocks.api.MaterialSpec;

import java.util.Collections;
import java.util.List;

/**
 * The selector item and its GUI. Texts are already colored.
 */
public final class SelectorSettings {

    private final int slot;
    private final MaterialSpec material;
    private final String name;
    private final List<String> lore;
    private final String guiTitle;
    private final int guiRows;

    public SelectorSettings(int slot, MaterialSpec material, String name, List<String> lore, String guiTitle, int guiRows) {
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.lore = Collections.unmodifiableList(lore);
        this.guiTitle = guiTitle;
        this.guiRows = guiRows;
    }

    public int getSlot() {
        return slot;
    }

    public MaterialSpec getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public String getGuiTitle() {
        return guiTitle;
    }

    public int getGuiRows() {
        return guiRows;
    }
}
