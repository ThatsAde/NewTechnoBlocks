package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.BlockWrapper;
import it.technoade.technoblocks.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class ItemsManager {

    public static final HashMap<ItemStack, String> item = new HashMap<>();

    public static void registerItems() {
        var section = Main.plugin.getConfig().getConfigurationSection("itemselectorgui");
        if (section == null) return;

        for (String s : section.getKeys(false)) {

            String matName = Main.plugin.getConfig().getString("itemselectorgui." + s + ".material");
            Material mat = BlockWrapper.get(Main.plugin.getConfig().getString("itemselectorgui." + s + ".material"), Material.STONE);

            if (mat == null) {
                Bukkit.getLogger().warning("§c[TechnoBlocks] Materiale non valido: " + matName);
                continue;
            }

            int quantity = Main.plugin.getConfig().getInt("itemselectorgui." + s + ".quantity");
            short damage = 0;

            item.put(
                    Utils.createItem(
                            quantity,
                            damage,
                            Utils.color(Main.plugin.getConfig().getString("itemselectorgui." + s + ".displayname")),
                            mat,
                            Collections.emptyList(),
                            Collections.emptyList(),
                            Collections.emptyList()),
                    s
            );
        }
    }


}
