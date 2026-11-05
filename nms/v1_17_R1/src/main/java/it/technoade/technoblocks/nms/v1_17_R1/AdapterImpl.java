package it.technoade.technoblocks.nms.v1_17_R1;

import it.technoade.technoblocks.common.flat.FlatAdapter;
import net.minecraft.core.BlockPosition;
import net.minecraft.network.protocol.game.PacketPlayOutBlockBreakAnimation;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public final class AdapterImpl extends FlatAdapter {

    public AdapterImpl(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void sendCrack(Player viewer, int sourceId, Location location, int stage) {
        BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ((CraftPlayer) viewer).getHandle().b.sendPacket(new PacketPlayOutBlockBreakAnimation(sourceId, position, stage));
    }
}
