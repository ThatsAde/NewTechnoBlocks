package it.technoade.technoblocks.common.flat;

import it.technoade.technoblocks.api.MaterialSpec;
import it.technoade.technoblocks.api.VersionAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Locale;

/**
 * Shared adapter logic for 1.13+ (flattened materials, BlockData, PDC).
 * Subclasses only add the crack animation.
 */
@SuppressWarnings("deprecation")
public abstract class FlatAdapter implements VersionAdapter {

    protected static final String TAG_KEY = "technoblocks";

    private final NamespacedKey key;

    protected FlatAdapter(Plugin plugin) {
        this.key = new NamespacedKey(plugin, TAG_KEY);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public ItemStack tag(ItemStack item, String value) {
        ItemStack copy = item.clone();
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return copy;
        meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, value);
        copy.setItemMeta(meta);
        return copy;
    }

    @Override
    public String readTag(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta().getPersistentDataContainer().get(key, PersistentDataType.STRING);
    }

    @Override
    public MaterialSpec parseMaterial(String spec) {
        return parseName(spec);
    }

    static MaterialSpec parseName(String spec) {
        if (spec == null) return null;
        String name = spec.trim().toUpperCase(Locale.ROOT);
        if (name.isEmpty()) return null;
        int colon = name.indexOf(':');
        Material material;
        if (colon >= 0) {
            int data;
            try {
                data = Integer.parseInt(name.substring(colon + 1));
            } catch (NumberFormatException e) {
                return null;
            }
            if (data < 0 || data > 15) return null;
            try {
                Material legacy = Material.getMaterial("LEGACY_" + name.substring(0, colon));
                if (legacy == null) return null;
                material = Bukkit.getUnsafe().fromLegacy(new MaterialData(legacy, (byte) data));
            } catch (Throwable t) {
                // legacy conversion missing on this server (future versions may drop it)
                return null;
            }
        } else {
            material = Material.matchMaterial(name);
        }
        if (material == null || material.name().startsWith("LEGACY_")) return null;
        return new MaterialSpec(material, (byte) 0);
    }

    @Override
    public boolean isAllowedBlock(MaterialSpec spec) {
        return BlockRules.isAllowed(spec.getMaterial());
    }

    @Override
    public ItemStack createItem(MaterialSpec spec, int amount) {
        return new ItemStack(spec.getMaterial(), amount);
    }

    @Override
    public String serializeState(BlockState state) {
        return state.getBlockData().getAsString();
    }

    @Override
    public void restoreState(Block block, String serialized) {
        block.setBlockData(Bukkit.createBlockData(serialized), false);
    }

    @Override
    public boolean matches(Block block, MaterialSpec spec) {
        return block.getType() == spec.getMaterial();
    }
}
