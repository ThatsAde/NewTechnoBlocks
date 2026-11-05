package it.technoade.technoblocks.api;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Everything that differs between server versions. Implementations live in the
 * nms/* and modern modules and expose a public constructor taking the plugin.
 */
public interface VersionAdapter {

    /** Sends a block break animation. Stage 0..9, -1 clears it. */
    void sendCrack(Player viewer, int sourceId, Location location, int stage);

    ItemStack getItemInHand(Player player);

    /** Returns a copy of the item carrying the hidden plugin tag. */
    ItemStack tag(ItemStack item, String value);

    /** Returns the hidden plugin tag, or null if the item has none. */
    String readTag(ItemStack item);

    /** Parses a config material ("WHITE_WOOL", "WOOL:0"), or null if unknown here. */
    MaterialSpec parseMaterial(String spec);

    /** True for full, solid, non-falling blocks without inventory or second half. */
    boolean isAllowedBlock(MaterialSpec spec);

    ItemStack createItem(MaterialSpec spec, int amount);

    String serializeState(BlockState state);

    /** Restores a state produced by {@link #serializeState} without physics. */
    void restoreState(Block block, String serialized);

    boolean matches(Block block, MaterialSpec spec);
}
