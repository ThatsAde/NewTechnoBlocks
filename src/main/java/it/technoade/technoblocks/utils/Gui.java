package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class Gui {

    public static Inventory selector(Player p) {
        Inventory gui = Bukkit.createInventory(p, Main.plugin.getConfig().getInt("itemselector.gui.guisize"), Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.gui.guititle"))));

        ItemsManager.item.keySet().forEach(itemStack -> {
            String a = ItemsManager.item.get(itemStack);
            int index = Main.plugin.getConfig().getInt("itemselectorgui." + a + ".position");
            gui.setItem(index, itemStack);
        });

        return gui;
    }

}
