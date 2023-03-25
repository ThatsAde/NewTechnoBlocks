package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public enum Items {
    ITEMSELECTOR(Utils.createItem(1, (short) 1,
            Utils.color(Objects.requireNonNull(Main.getPlugin().getConfig().getString("itemselector.displayname"))),
            Material.getMaterial(Objects.requireNonNull(Main.getPlugin().getConfig().getString("itemselector.material"))), null, null, null));

    private final ItemStack itemStack;

    Items(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
