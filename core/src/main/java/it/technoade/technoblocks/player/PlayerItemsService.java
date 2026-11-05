package it.technoade.technoblocks.player;

import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.config.BlockEntry;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.config.SelectorSettings;
import it.technoade.technoblocks.data.DataStore;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Gives and removes the selector and the selected block.
 */
public final class PlayerItemsService {

    private final VersionAdapter adapter;
    private final DataStore data;

    public PlayerItemsService(VersionAdapter adapter, DataStore data) {
        this.adapter = adapter;
        this.data = data;
    }

    /** Gives the items in enabled worlds and removes them everywhere else. */
    public void apply(Player player, PluginSettings settings) {
        strip(player);
        if (settings.getWorlds().isEnabled(player.getWorld().getName())) give(player, settings);
    }

    public void give(Player player, PluginSettings settings) {
        player.getInventory().setItem(settings.getSelector().getSlot(), taggedSelector(settings));
        giveBlock(player, settings);
    }

    /** Puts the selected block (or nothing, if none is usable) in the placed-blocks slot. */
    public void giveBlock(Player player, PluginSettings settings) {
        BlockEntry entry = selectedEntry(player, settings);
        player.getInventory().setItem(settings.getPlacedSlot(), entry == null ? null : taggedBlockItem(entry));
    }

    public void strip(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            if (ItemProtection.isPluginTag(adapter.readTag(contents[i]))) inv.setItem(i, null);
        }
    }

    /** The saved choice if it still exists and is permitted, otherwise the first permitted block. */
    public BlockEntry selectedEntry(Player player, PluginSettings settings) {
        BlockEntry chosen = settings.getBlocks().get(data.selected(player.getUniqueId()));
        if (chosen != null && canUse(player, chosen)) return chosen;
        for (BlockEntry entry : settings.getBlocks().values()) {
            if (canUse(player, entry)) return entry;
        }
        return null;
    }

    public static boolean canUse(Player player, BlockEntry entry) {
        return entry.getPermission() == null || player.hasPermission(entry.getPermission());
    }

    public ItemStack taggedBlockItem(BlockEntry entry) {
        ItemStack item = named(adapter.createItem(entry.getMaterial(), entry.getAmount()), entry.getName(), entry.getLore());
        return adapter.tag(item, ItemProtection.BLOCK_PREFIX + entry.getKey());
    }

    public ItemStack taggedSelector(PluginSettings settings) {
        SelectorSettings selector = settings.getSelector();
        ItemStack item = named(adapter.createItem(selector.getMaterial(), 1), selector.getName(), selector.getLore());
        return adapter.tag(item, ItemProtection.SELECTOR);
    }

    private static ItemStack named(ItemStack item, String name, List<String> lore) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        if (name != null) meta.setDisplayName(name);
        if (!lore.isEmpty()) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
}
