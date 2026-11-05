package it.technoade.technoblocks;

import it.technoade.technoblocks.api.MaterialSpec;
import it.technoade.technoblocks.api.VersionAdapter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/** Server-free adapter for unit tests. */
public class FakeAdapter implements VersionAdapter {

    @Override
    public void sendCrack(Player viewer, int sourceId, Location location, int stage) {
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return null;
    }

    @Override
    public ItemStack tag(ItemStack item, String value) {
        return item;
    }

    @Override
    public String readTag(ItemStack item) {
        return null;
    }

    @Override
    public MaterialSpec parseMaterial(String spec) {
        Material m = spec == null ? null : Material.matchMaterial(spec);
        return m == null ? null : new MaterialSpec(m, (byte) 0);
    }

    @Override
    public boolean isAllowedBlock(MaterialSpec spec) {
        Material m = spec.getMaterial();
        return m.isBlock() && m.isSolid() && !m.hasGravity() && m != Material.CHEST;
    }

    @Override
    public ItemStack createItem(MaterialSpec spec, int amount) {
        return null;
    }

    @Override
    public String serializeState(BlockState state) {
        return null;
    }

    @Override
    public void restoreState(Block block, String serialized) {
    }

    @Override
    public boolean matches(Block block, MaterialSpec spec) {
        return false;
    }
}
