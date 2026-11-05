package it.technoade.technoblocks.listener;

import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.block.TemporaryBlockManager;
import it.technoade.technoblocks.config.PluginSettings;
import it.technoade.technoblocks.gui.SelectorGui;
import it.technoade.technoblocks.player.ItemProtection;
import it.technoade.technoblocks.player.PlayerItemsService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

import java.util.function.Supplier;

/**
 * Gives/removes plugin items as players move between worlds and opens the selector.
 */
public final class PlayerListener implements Listener {

    private final Plugin plugin;
    private final VersionAdapter adapter;
    private final Supplier<PluginSettings> settings;
    private final TemporaryBlockManager manager;
    private final PlayerItemsService items;

    public PlayerListener(Plugin plugin, VersionAdapter adapter, Supplier<PluginSettings> settings,
                          TemporaryBlockManager manager, PlayerItemsService items) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.settings = settings;
        this.manager = manager;
        this.items = items;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        later(e.getPlayer(), true);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        later(e.getPlayer(), true);
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent e) {
        later(e.getPlayer(), true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        later(e.getPlayer(), false);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!ItemProtection.SELECTOR.equals(adapter.readTag(e.getItem()))) return;
        e.setCancelled(true);
        SelectorGui.open(e.getPlayer(), settings.get(), items);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDrop(PlayerDropItemEvent e) {
        if (ItemProtection.isPluginTag(adapter.readTag(e.getItemDrop().getItemStack()))) e.setCancelled(true);
    }

    /** One tick later, so the player is fully in the new world/location. */
    private void later(final Player player, final boolean applyItems) {
        Bukkit.getScheduler().runTask(plugin, new Runnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;
                if (applyItems) items.apply(player, settings.get());
                manager.resendTo(player);
            }
        });
    }
}
