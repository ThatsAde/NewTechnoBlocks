package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class ItemsManager {

    public static final HashMap<ItemStack, String> item = new HashMap<>();

    public static void registerItems() {
        ArrayList<String> configItems = new ArrayList<>(Objects.requireNonNull(Main.plugin.getConfig().getConfigurationSection("itemselectorgui")).getKeys(false));
        configItems.forEach(s -> item.put(Utils.createItem(
                Main.plugin.getConfig().getInt("itemselectorgui." + s + ".quantity"),
                (short) 1, Utils.color(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselectorgui." + s + ".displayname"))),
                Material.getMaterial(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselectorgui." + s + ".material"))),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList()), s));
    }

}
