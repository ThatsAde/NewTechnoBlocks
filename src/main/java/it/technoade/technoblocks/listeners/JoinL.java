package it.technoade.technoblocks.listeners;

import it.technoade.technoblocks.Main;
import it.technoade.technoblocks.utils.Items;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinL implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        e.getPlayer().getInventory().setItem(Main.getPlugin().getConfig().getInt("itemselector.position"), Items.ITEMSELECTOR.getItem());
    }

}
