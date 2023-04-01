package it.technoade.technoblocks.tasks;

import it.technoade.technoblocks.Main;
import it.technoade.technoblocks.utils.*;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class Events implements Listener {

    private static final HashMap<Player, ItemStack> ciccio = new HashMap<>();
    public static final HashSet<Block> saveStack = new HashSet<>();

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent e) {
        final Player p = e.getPlayer();
        if(e.getItemInHand().equals(Items.SELECTOR.getItem())) {e.setCancelled(true); p.getInventory().setItem(Main.plugin.getConfig().getInt("itemselector.position"), Items.SELECTOR.getItem()); return;}
        if(ItemsManager.item.containsKey(e.getItemInHand())) {
            if(!p.getGameMode().equals(GameMode.CREATIVE)) {
                p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position") , ciccio.getOrDefault(p, (ItemStack) ItemsManager.item.keySet().toArray()[0]));
            }
            saveStack.add(e.getBlockPlaced());
            Bukkit.getScheduler().runTaskLater(Main.plugin, () -> {
                e.getBlockPlaced().setType(Material.AIR);
                if(saveStack.contains(e.getBlockPlaced())) {
                    saveStack.remove(e.getBlockPlaced());
                }
            }, Main.plugin.getConfig().getLong("blocks.remove")*20);
        }
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent e) {
        final Player p = e.getPlayer();
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) {
            final ItemStack item = p.getInventory().getItemInMainHand();
            if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
                if (item.getItemMeta().getDisplayName().equals(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.displayname"))))) {
                    p.openInventory(Gui.selector(p));
                }
            }
        }
    }

    @EventHandler
    public void onDrop(final PlayerDropItemEvent e) {
        final Player p = e.getPlayer();
        final ItemStack iteminhand = e.getItemDrop().getItemStack();
        if(ItemsManager.item.containsKey(e.getItemDrop().getItemStack())) {
            e.setCancelled(true);
        }
        if(iteminhand.hasItemMeta() && iteminhand.getItemMeta().hasDisplayName()) {
            if(iteminhand.getItemMeta().getDisplayName().equals(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.displayname"))))) {
                e.setCancelled(true);
            }
        }

    }

    @EventHandler
    public void leaveEvent(final PlayerQuitEvent e) {
        ciccio.remove(e.getPlayer());
    }

    @EventHandler
    public void respawnEvent(final PlayerRespawnEvent e) {
        final Player p = e.getPlayer();
        p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position") , ciccio.getOrDefault(p, (ItemStack) ItemsManager.item.keySet().toArray()[0]));
        p.getInventory().setItem(Main.plugin.getConfig().getInt("itemselector.position"), Items.SELECTOR.getItem());
    }

    @EventHandler
    public void onJoinEvent(final PlayerJoinEvent e) {
        final Player p = e.getPlayer();
        p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position") , ciccio.getOrDefault(p, (ItemStack) ItemsManager.item.keySet().toArray()[0]));
        p.getInventory().setItem(Main.plugin.getConfig().getInt("itemselector.position"), Items.SELECTOR.getItem());
    }

    @EventHandler
    public void onInvClick(final InventoryClickEvent e) {
        final Player p = (Player) e.getWhoClicked();
        if(e.getView().getTitle().equals(Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.gui.guititle"))))) {
            e.setCancelled(true);
            final ItemStack itemcliccato = e.getCurrentItem();
            if(itemcliccato == null) return;
            final String cicciogamer = ItemsManager.item.get(itemcliccato);
            if(Main.plugin.getConfig().getString("itemselectorgui." + cicciogamer + ".permission") != null) {
                if(p.hasPermission(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselectorgui." + cicciogamer + ".permission")))) {
                    p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"), itemcliccato);
                    p.sendMessage(Messages.SELECTEDITEM.getString());
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 2F);
                    ciccio.put(p, itemcliccato);
                    p.closeInventory();
                    e.setCancelled(true);
                } else {
                    p.sendMessage(Messages.NO_PERMS.getString());
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 2F);
                    e.setCancelled(true);
                }
            } else {
                e.setCancelled(true);
                ciccio.put(p, itemcliccato);
                p.getInventory().setItem(Main.plugin.getConfig().getInt("blocks.position"), itemcliccato);
                p.sendMessage(Messages.SELECTEDITEM.getString());
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_YES, 1F, 2F);
            }
            e.setCancelled(true);
        }
    }

}
