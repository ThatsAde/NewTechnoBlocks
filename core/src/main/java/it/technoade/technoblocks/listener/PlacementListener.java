package it.technoade.technoblocks.listener;

import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.block.TemporaryBlockManager;
import it.technoade.technoblocks.config.BlockEntry;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.data.DataStore;
import it.technoade.technoblocks.player.ItemProtection;
import it.technoade.technoblocks.player.PlayerItemsService;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/**
 * Turns placed plugin blocks into temporary blocks and refills the slot.
 */
public final class PlacementListener implements Listener {

    private final Plugin plugin;
    private final VersionAdapter adapter;
    private final Supplier<PluginSettings> settings;
    private final TemporaryBlockManager manager;
    private final DataStore data;
    private final PlayerItemsService items;

    public PlacementListener(Plugin plugin, VersionAdapter adapter, Supplier<PluginSettings> settings,
                             TemporaryBlockManager manager, DataStore data, PlayerItemsService items) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.settings = settings;
        this.manager = manager;
        this.data = data;
        this.items = items;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlace(BlockPlaceEvent e) {
        String tag = adapter.readTag(e.getItemInHand());
        if (tag == null) return;
        if (ItemProtection.SELECTOR.equals(tag)) {
            e.setCancelled(true);
            return;
        }
        String key = ItemProtection.blockKey(tag);
        if (key == null) return;

        final Player player = e.getPlayer();
        PluginSettings s = settings.get();
        BlockEntry entry = s.getBlocks().get(key);
        if (entry == null || !s.getWorlds().isEnabled(player.getWorld().getName())) {
            e.setCancelled(true);
            return;
        }

        manager.register(e.getBlockPlaced(), e.getBlockReplacedState(), entry.getMaterial(), entry.getDurationMillis());
        data.increment(player.getUniqueId());

        if (player.getGameMode() != GameMode.CREATIVE) {
            Bukkit.getScheduler().runTask(plugin, new Runnable() {
                @Override
                public void run() {
                    PluginSettings current = settings.get();
                    if (player.isOnline() && current.getWorlds().isEnabled(player.getWorld().getName())) {
                        items.giveBlock(player, current);
                    }
                }
            });
        }
    }
}
