package it.technoade.technoblocks.common.legacy;

import it.technoade.technoblocks.api.MaterialSpec;
import it.technoade.technoblocks.api.VersionAdapter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * Shared adapter logic for 1.8-1.12 (numeric ids and data values).
 * NMS modules add the crack packet and NBT tagging.
 */
@SuppressWarnings("deprecation")
public abstract class LegacyAdapter implements VersionAdapter {

    protected LegacyAdapter(Plugin plugin) {
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getItemInHand();
    }

    @Override
    public MaterialSpec parseMaterial(String spec) {
        return LegacyMaterials.parse(spec);
    }

    @Override
    public boolean isAllowedBlock(MaterialSpec spec) {
        return LegacyMaterials.isAllowed(spec);
    }

    @Override
    public ItemStack createItem(MaterialSpec spec, int amount) {
        return new ItemStack(spec.getMaterial(), amount, spec.getData());
    }

    @Override
    public String serializeState(BlockState state) {
        return state.getType().name() + ":" + state.getRawData();
    }

    @Override
    public void restoreState(Block block, String serialized) {
        MaterialSpec spec = LegacyMaterials.parse(serialized);
        if (spec == null) spec = new MaterialSpec(Material.AIR, (byte) 0);
        block.setTypeIdAndData(spec.getMaterial().getId(), spec.getData(), false);
    }

    /** Type only: the placed data value can differ from the item's (e.g. log orientation bits). */
    @Override
    public boolean matches(Block block, MaterialSpec spec) {
        return block.getType() == spec.getMaterial();
    }
}
