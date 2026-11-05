package it.technoade.technoblocks.listener;

import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.config.BlockEntry;
import it.technoade.technoblocks.config.Messages;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.data.DataStore;
import it.technoade.technoblocks.gui.SelectorHolder;
import it.technoade.technoblocks.player.ItemProtection;
import it.technoade.technoblocks.player.PlayerItemsService;
import org.bukkit.GameMode;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/**
 * Selector GUI clicks and keeping plugin items in their slots.
 * Never calls InventoryEvent#getView(): InventoryView is a class on old servers and an interface on new ones.
 */
public final class InventoryListener implements Listener {

    private final VersionAdapter adapter;
    private final Supplier<PluginSettings> settings;
    private final Supplier<Messages> messages;
    private final DataStore data;
    private final PlayerItemsService items;

    public InventoryListener(VersionAdapter adapter, Supplier<PluginSettings> settings, Supplier<Messages> messages,
                             DataStore data, PlayerItemsService items) {
        this.adapter = adapter;
        this.settings = settings;
        this.messages = messages;
        this.data = data;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onClick(InventoryClickEvent e) {
        HumanEntity who = e.getWhoClicked();
        if (!(who instanceof Player)) return;
        Player player = (Player) who;
        Inventory top = e.getInventory();

        if (top.getHolder() instanceof SelectorHolder) {
            e.setCancelled(true);
            if (e.getRawSlot() >= 0 && e.getRawSlot() < top.getSize()) select(player, adapter.readTag(e.getCurrentItem()));
            return;
        }

        if (player.getGameMode() == GameMode.CREATIVE) return;
        String hotbarTag = null;
        if (e.getHotbarButton() >= 0) hotbarTag = adapter.readTag(player.getInventory().getItem(e.getHotbarButton()));
        if (ItemProtection.shouldCancelClick(adapter.readTag(e.getCurrentItem()), adapter.readTag(e.getCursor()), hotbarTag)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDrag(InventoryDragEvent e) {
        if (e.getInventory().getHolder() instanceof SelectorHolder) {
            e.setCancelled(true);
            return;
        }
        if (e.getWhoClicked().getGameMode() == GameMode.CREATIVE) return;
        if (ItemProtection.isPluginTag(adapter.readTag(e.getOldCursor()))) e.setCancelled(true);
    }

    private void select(Player player, String tag) {
        String key = ItemProtection.blockKey(tag);
        if (key == null) return;
        PluginSettings s = settings.get();
        BlockEntry entry = s.getBlocks().get(key);
        if (entry == null) return;
        if (!PlayerItemsService.canUse(player, entry)) {
            player.sendMessage(messages.get().get("no-permission"));
            return;
        }
        data.select(player.getUniqueId(), key);
        if (s.getWorlds().isEnabled(player.getWorld().getName())) items.giveBlock(player, s);
        player.sendMessage(messages.get().get("block-selected"));
        player.closeInventory();
    }

    /**
     * Blocks moving plugin items to the off hand (F key). The event only exists on 1.9+,
     * so it is registered by name.
     */
    public void registerSwapHandGuard(Plugin plugin) {
        final Class<? extends Event> type;
        try {
            type = Class.forName("org.bukkit.event.player.PlayerSwapHandItemsEvent").asSubclass(Event.class);
        } catch (ClassNotFoundException e) {
            return;
        }
        plugin.getServer().getPluginManager().registerEvent(type, this, EventPriority.HIGH, new EventExecutor() {
            @Override
            public void execute(Listener listener, Event event) {
                if (!type.isInstance(event)) return;
                Player player = ((PlayerEvent) event).getPlayer();
                if (ItemProtection.isPluginTag(adapter.readTag(adapter.getItemInHand(player)))) {
                    ((Cancellable) event).setCancelled(true);
                }
            }
        }, plugin, true);
    }
}
