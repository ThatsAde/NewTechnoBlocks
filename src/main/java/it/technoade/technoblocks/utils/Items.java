package it.technoade.technoblocks.utils;

import it.technoade.technoblocks.Main;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Objects;

public enum Items {
    SELECTOR(Utils.createItem(1, (short) 1,
            Messages.SELECTORTITLE.getString(),
            Material.getMaterial(Objects.requireNonNull(Main.plugin.getConfig().getString("itemselector.material"))),
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList()));

    private final ItemStack itemStack;

    Items(ItemStack itemstack) {
        this.itemStack = itemstack;
    }

    public ItemStack getItem() {
        return this.itemStack;
    }
}
