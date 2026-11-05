package it.technoade.technoblocks.nms.v1_16_R3;

import it.technoade.technoblocks.common.flat.FlatAdapter;
import net.minecraft.server.v1_16_R3.BlockPosition;
import net.minecraft.server.v1_16_R3.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class AdapterImpl extends FlatAdapter {

    public AdapterImpl(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void sendCrack(Player viewer, int sourceId, Location location, int stage) {
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftPlayer) viewer).getHandle().playerConnection
                .sendPacket(new PacketPlayOutBlockBreakAnimation(sourceId, position, stage));
    }
}
