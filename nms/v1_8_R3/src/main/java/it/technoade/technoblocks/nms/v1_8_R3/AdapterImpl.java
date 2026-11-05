package it.technoade.technoblocks.nms.v1_8_R3;

import it.technoade.technoblocks.common.legacy.LegacyAdapter;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public final class AdapterImpl extends LegacyAdapter {

    private static final String TAG_KEY = "technoblocks";

    public AdapterImpl(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void sendCrack(Player viewer, int sourceId, Location location, int stage) {
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftPlayer) viewer).getHandle().playerConnection
                .sendPacket(new PacketPlayOutBlockBreakAnimation(sourceId, position, stage));
    }
    @Override
    public ItemStack tag(ItemStack item, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null) return item.clone();
        NBTTagCompound tag = nms.hasTag() ? nms.getTag() : new NBTTagCompound();
        tag.setString(TAG_KEY, value);
        nms.setTag(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }

    @Override
    public String readTag(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return null;
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (nms == null || !nms.hasTag()) return null;
        NBTTagCompound tag = nms.getTag();
        return tag.hasKey(TAG_KEY) ? tag.getString(TAG_KEY) : null;
    }
}
