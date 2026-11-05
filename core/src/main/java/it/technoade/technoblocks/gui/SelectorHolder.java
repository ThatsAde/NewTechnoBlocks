package it.technoade.technoblocks.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Marks the selector GUI, so it is recognised without comparing titles.
 */
public final class SelectorHolder implements InventoryHolder {

    private Inventory inventory;

    void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
