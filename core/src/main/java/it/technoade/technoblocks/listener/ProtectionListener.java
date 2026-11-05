package it.technoade.technoblocks.listener;

import it.technoade.technoblocks.block.TemporaryBlockManager;
import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import java.util.Iterator;
import java.util.List;

/**
 * Keeps temporary blocks from being broken, moved or dropped before they expire.
 */
public final class ProtectionListener implements Listener {

    private final TemporaryBlockManager manager;

    public ProtectionListener(TemporaryBlockManager manager) {
        this.manager = manager;
    }

    /** Only creative players may break temporary blocks early; creative drops nothing. */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent e) {
        if (manager.isTemporary(e.getBlock()) && e.getPlayer().getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
        }
    }

    /** Forgets the block only once no other plugin can cancel the break any more. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreakFinal(BlockBreakEvent e) {
        if (manager.isTemporary(e.getBlock())) manager.forget(e.getBlock());
    }

    /** The world is still accessible here, so its blocks can be restored before it is saved. */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent e) {
        manager.restoreWorld(e.getWorld().getName());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent e) {
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent e) {
        handleExplosion(e.blockList());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent e) {
        if (anyTemporary(e.getBlocks())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent e) {
        if (anyTemporary(e.getBlocks())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBurn(BlockBurnEvent e) {
        if (manager.isTemporary(e.getBlock())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityChangeBlock(EntityChangeBlockEvent e) {
        if (manager.isTemporary(e.getBlock())) e.setCancelled(true);
    }

    /** Takes temporary blocks out of the explosion and restores them without drops. */
    private void handleExplosion(List<Block> blocks) {
        Iterator<Block> it = blocks.iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (manager.isTemporary(block)) {
                it.remove();
                manager.removeNow(block);
            }
        }
    }

    private boolean anyTemporary(List<Block> blocks) {
        for (Block block : blocks) {
            if (manager.isTemporary(block)) return true;
        }
        return false;
    }
}
