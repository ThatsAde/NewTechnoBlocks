package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;
import it.technoade.technoblocks.api.VersionAdapter;
import it.technoade.technoblocks.config.PluginSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Drives every temporary block: crack updates, expiry and persistence for crash recovery.
 * Main thread only.
 */
public final class TemporaryBlockManager {

    private static final long TICK_PERIOD = 2L;
    private static final long SAVE_PERIOD = 40L;

    private final Plugin plugin;
    private final VersionAdapter adapter;
    private final ActiveBlocksStore store;
    private final TemporaryBlockRegistry registry = new TemporaryBlockRegistry();

    private PluginSettings settings;
    private BukkitTask tickTask;
    private BukkitTask saveTask;
    private boolean dirty;
    private boolean crackErrorLogged;
    /** Lines from a crash whose world wasn't loaded; written back on every save. */
    private List<String> keptFromCrash = new ArrayList<String>();

    public TemporaryBlockManager(Plugin plugin, VersionAdapter adapter, ActiveBlocksStore store) {
        this.plugin = plugin;
        this.adapter = adapter;
        this.store = store;
    }

    public void start(PluginSettings settings) {
        this.settings = settings;
        tickTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                tick();
            }
        }, TICK_PERIOD, TICK_PERIOD);
        saveTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                flush();
            }
        }, SAVE_PERIOD, SAVE_PERIOD);
    }

    public void applySettings(PluginSettings settings) {
        this.settings = settings;
    }

    public void stop() {
        if (tickTask != null) tickTask.cancel();
        if (saveTask != null) saveTask.cancel();
        tickTask = null;
        saveTask = null;
    }

    public void register(Block placed, BlockState replaced, MaterialSpec spec, long durationMillis) {
        BlockPos pos = BlockPos.of(placed);
        TemporaryBlock previous = registry.get(pos);
        if (previous != null) clearCrack(previous);
        registry.add(pos, spec, adapter.serializeState(replaced), System.currentTimeMillis(), durationMillis);
        dirty = true;
    }

    public boolean isTemporary(Block block) {
        return registry.get(BlockPos.of(block)) != null;
    }

    /** Clears the crack and restores what was there before. */
    public void removeNow(Block block) {
        TemporaryBlock temp = registry.remove(BlockPos.of(block));
        if (temp != null) finish(temp);
    }

    /** Stops tracking a block that something else already removed. */
    public void forget(Block block) {
        TemporaryBlock temp = registry.remove(BlockPos.of(block));
        if (temp != null) {
            clearCrack(temp);
            dirty = true;
        }
    }

    /** Restores every temporary block now. @return how many were restored */
    public int restoreAll() {
        List<TemporaryBlock> all = registry.drainAll();
        for (TemporaryBlock temp : all) finish(temp);
        store.writeLinesSync(persistedLines());
        dirty = false;
        return all.size();
    }

    /** Sends the current crack stage of nearby blocks to a player who just arrived. */
    public void resendTo(Player player) {
        if (!settings.isCracksEnabled()) return;
        double max = (double) settings.getViewDistance() * settings.getViewDistance();
        for (TemporaryBlock temp : registry.all()) {
            if (temp.getLastSentStage() < 0) continue;
            Location loc = location(temp);
            if (loc == null || loc.getWorld() != player.getWorld()) continue;
            if (player.getLocation().distanceSquared(loc) <= max) {
                send(player, temp, loc, temp.getLastSentStage());
            }
        }
    }

    /** Cleans up blocks left by a crash; lines for worlds that aren't loaded are kept for later. */
    public void recoverFromCrash() {
        List<String> lines = store.readLines();
        int restored = 0;
        for (String line : lines) {
            ActiveBlocksStore.Entry entry = ActiveBlocksStore.decode(line);
            if (entry == null) continue;
            World world = Bukkit.getWorld(entry.getPos().getWorld());
            if (world == null) continue;
            MaterialSpec placed = adapter.parseMaterial(entry.getPlaced());
            Block block = world.getBlockAt(entry.getPos().getX(), entry.getPos().getY(), entry.getPos().getZ());
            if (placed != null && adapter.matches(block, placed)) {
                adapter.restoreState(block, entry.getReplacedState());
                restored++;
            }
        }
        List<String> kept = ActiveBlocksStore.linesToKeep(lines, new java.util.function.Predicate<String>() {
            @Override
            public boolean test(String name) {
                return Bukkit.getWorld(name) != null;
            }
        });
        if (restored > 0) plugin.getLogger().info("Removed " + restored + " temporary blocks left by a crash");
        if (!kept.isEmpty()) {
            plugin.getLogger().warning(kept.size() + " temporary blocks are in worlds that are not loaded; "
                    + "they will be cleaned up on a later start");
        }
        keptFromCrash = kept;
        store.writeLinesSync(persistedLines());
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (TemporaryBlock temp : registry.pollExpired(now)) finish(temp);
        if (!settings.isCracksEnabled()) return;
        for (TemporaryBlock temp : registry.all()) {
            int stage = CrackMath.stage(now - temp.getPlacedAt(), temp.getExpiresAt() - temp.getPlacedAt());
            if (CrackMath.shouldSend(stage, temp.getLastSentStage(), now, temp.getLastSentAt())) {
                sendToViewers(temp, stage);
                temp.markSent(stage, now);
            }
        }
    }

    private void flush() {
        if (!dirty) return;
        dirty = false;
        store.writeLinesAsync(plugin, persistedLines());
    }

    private List<String> persistedLines() {
        List<String> lines = new ArrayList<String>(keptFromCrash);
        for (TemporaryBlock temp : registry.all()) lines.add(ActiveBlocksStore.encode(temp));
        return lines;
    }

    private void finish(TemporaryBlock temp) {
        clearCrack(temp);
        dirty = true;
        Location loc = location(temp);
        if (loc == null) return;
        Block block = loc.getBlock();
        if (adapter.matches(block, temp.getPlaced())) {
            adapter.restoreState(block, temp.getReplacedState());
        }
    }

    private void clearCrack(TemporaryBlock temp) {
        if (temp.getLastSentStage() >= 0) sendToViewers(temp, -1);
    }

    private void sendToViewers(TemporaryBlock temp, int stage) {
        Location loc = location(temp);
        if (loc == null) return;
        double max = (double) settings.getViewDistance() * settings.getViewDistance();
        for (Player player : loc.getWorld().getPlayers()) {
            if (player.getLocation().distanceSquared(loc) <= max) send(player, temp, loc, stage);
        }
    }

    private void send(Player player, TemporaryBlock temp, Location loc, int stage) {
        try {
            adapter.sendCrack(player, temp.getSourceId(), loc, stage);
        } catch (RuntimeException e) {
            if (!crackErrorLogged) {
                crackErrorLogged = true;
                plugin.getLogger().log(Level.WARNING, "Could not send crack animation", e);
            }
        }
    }

    private static Location location(TemporaryBlock temp) {
        World world = Bukkit.getWorld(temp.getPos().getWorld());
        if (world == null) return null;
        return new Location(world, temp.getPos().getX(), temp.getPos().getY(), temp.getPos().getZ());
    }
}
