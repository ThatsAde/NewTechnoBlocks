package it.technoade.technoblocks.tasks;

import it.technoade.technoblocks.Main;
import it.technoade.technoblocks.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;

public class Events implements Listener {

    private static final HashMap<Player, ItemStack> selectedItem = new HashMap<>();
    public static final HashSet<Block> temporaryBlocks = new HashSet<>();

    /** ============================
     *        EVENTI BLOCCO
     * ============================ */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        ItemStack hand = getHandItem(p);
        if (hand == null) return;

        // Blocchi Selector
        if (hand.equals(Items.SELECTOR.getItem())) {
            e.setCancelled(true);
            p.getInventory().setItem(Main.plugin.getConfig().getInt("itemselector.position"), Items.SELECTOR.getItem());
            return;
        }

        // Blocco cassa vietato
        if (e.getBlockPlaced().getType() == Material.CHEST) {
            e.setCancelled(true);
            return;
        }

        // Blocchi definiti in itemselectorgui
        if (ItemsManager.item.containsKey(hand)) {
            if (!p.getGameMode().equals(GameMode.CREATIVE)) {
                // Rimetto il blocco principale dopo il piazzamento
                p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"),
                        selectedItem.getOrDefault(p, (ItemStack) ItemsManager.item.keySet().toArray()[0]));
            }

            temporaryBlocks.add(e.getBlockPlaced());
            Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
                e.getBlockPlaced().setType(Material.AIR);
                temporaryBlocks.remove(e.getBlockPlaced());
            }, Main.plugin.getConfig().getLong("blocks.remove") * 20);
        }
    }


    /** ============================
     *        EVENTI INTERAZIONE
     * ============================ */
    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_AIR) return;

        ItemStack hand = getHandItem(p);
        if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasDisplayName()) return;

        if (hand.getItemMeta().getDisplayName().equals(Utils.color(
                Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.displayname"))))) {
            p.openInventory(Gui.selector(p));
        }
    }

    /** ============================
     *        DROP EVENT
     * ============================ */
    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player p = e.getPlayer();
        ItemStack dropped = e.getItemDrop().getItemStack();
        if (dropped == null) return;

        if (ItemsManager.item.containsKey(dropped)) {
            e.setCancelled(true);
        }
        if (dropped.hasItemMeta() && dropped.getItemMeta().hasDisplayName() &&
                dropped.getItemMeta().getDisplayName().equals(Utils.color(
                        Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.displayname"))))) {
            e.setCancelled(true);
        }
    }

    /** ============================
     *        JOIN / QUIT / RESPAWN
     * ============================ */
    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        setPlayerItems(p);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player p = e.getPlayer();
        setPlayerItems(p);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        selectedItem.remove(e.getPlayer());
    }

    /** ============================
     *        INVENTORY CLICK
     * ============================ */
    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        String guiTitle = Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.gui.guititle")));

        // Su 1.8.8 getView() ritorna InventoryView come classe

        if (Bukkit.getServer().getVersion().contains("1.8")) {
            e.setCancelled(true);
            final ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            final String itemKey = ItemsManager.item.get(clickedItem);
            if (itemKey == null) return;

            String permissionPath = "itemselectorgui." + itemKey + ".permission";
            String permission = Main.plugin.getConfig().getString(permissionPath);

            if (permission != null && !p.hasPermission(permission)) {
                p.sendMessage(Messages.NO_PERMS.getString());
                return;
            }

            selectedItem.put(p, clickedItem);
            p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"), clickedItem);
            p.sendMessage(Messages.SELECTEDITEM.getString());
            p.closeInventory();
        } else {
            if (e.getView().getTitle().equals(guiTitle)) {
                e.setCancelled(true);
                final ItemStack clickedItem = e.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

                final String itemKey = ItemsManager.item.get(clickedItem);
                if (itemKey == null) return;

                String permissionPath = "itemselectorgui." + itemKey + ".permission";
                String permission = Main.plugin.getConfig().getString(permissionPath);

                if (permission != null && !p.hasPermission(permission)) {
                    p.sendMessage(Messages.NO_PERMS.getString());
                    return;
                }

                selectedItem.put(p, clickedItem);
                p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"), clickedItem);
                p.sendMessage(Messages.SELECTEDITEM.getString());
                p.closeInventory();
            }
        }
    }


    /** ============================
     *        WRAPPER HAND ITEM
     * ============================ */
    public static ItemStack getHandItem(Player p) {
        try {
            return p.getInventory().getItemInMainHand(); // 1.9+
        } catch (NoSuchMethodError e) {
            return p.getItemInHand(); // 1.8
        }
    }

    /** ============================
     *        UTILS
     * ============================ */
    private void setPlayerItems(Player p) {
        p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"),
                selectedItem.getOrDefault(p, (ItemStack) ItemsManager.item.keySet().toArray()[0]));
        p.getInventory().setItem(Main.plugin.getConfig().getInt("itemselector.position"), Items.SELECTOR.getItem());
    }
}
