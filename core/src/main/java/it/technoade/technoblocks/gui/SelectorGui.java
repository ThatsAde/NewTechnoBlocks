package it.technoade.technoblocks.gui;

import it.technoade.technoblocks.config.BlockEntry;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.player.PlayerItemsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/**
 * The block selector GUI.
 */
public final class SelectorGui {

    /** 1.8 rejects inventory titles longer than 32 characters. */
    private static final int MAX_TITLE = 32;

    private SelectorGui() {
    }

    public static Inventory open(Player player, PluginSettings settings, PlayerItemsService items) {
        String title = settings.getSelector().getGuiTitle();
        if (title.length() > MAX_TITLE) title = title.substring(0, MAX_TITLE);
        SelectorHolder holder = new SelectorHolder();
        Inventory inv = Bukkit.createInventory(holder, settings.getSelector().getGuiRows() * 9, title);
        holder.setInventory(inv);
        for (BlockEntry entry : settings.getBlocks().values()) {
            inv.setItem(entry.getGuiSlot(), items.taggedBlockItem(entry));
        }
        player.openInventory(inv);
        return inv;
    }
}
