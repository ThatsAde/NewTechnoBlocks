package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * active-blocks.yml: positions of live temporary blocks, so a crash can be cleaned up on the next start.
 */
public final class ActiveBlocksStore {

    private static final String KEY = "blocks";

    private final File file;
    private final AtomicLong generation = new AtomicLong();

    public ActiveBlocksStore(File file) {
        this.file = file;
    }

    /** "world;x;y;z|placed|replacedState"; placed omits ":0" so it parses on every version. */
    public static String encode(TemporaryBlock block) {
        MaterialSpec placed = block.getPlaced();
        String material = placed.getData() == 0 ? placed.getMaterial().name() : placed.toString();
        return block.getPos().serialize() + "|" + material + "|" + block.getReplacedState();
    }

    /** @return the entry, or null if the line is malformed */
    public static Entry decode(String line) {
        String[] parts = line.split("\\|", 3);
        if (parts.length != 3) return null;
        BlockPos pos = BlockPos.parse(parts[0]);
        if (pos == null || parts[1].isEmpty() || parts[2].isEmpty()) return null;
        return new Entry(pos, parts[1], parts[2]);
    }

    /** Lines whose world is not loaded yet; malformed lines are dropped. */
    public static List<String> linesToKeep(List<String> lines, java.util.function.Predicate<String> worldLoaded) {
        List<String> kept = new ArrayList<String>();
        for (String line : lines) {
            Entry entry = decode(line);
            if (entry != null && !worldLoaded.test(entry.getPos().getWorld())) kept.add(line);
        }
        return kept;
    }

    public List<String> readLines() {
        if (!file.exists()) return Collections.emptyList();
        return YamlConfiguration.loadConfiguration(file).getStringList(KEY);
    }

    public void writeLinesSync(List<String> lines) {
        write(generation.incrementAndGet(), lines);
    }

    public void writeLinesAsync(Plugin plugin, final List<String> lines) {
        final long gen = generation.incrementAndGet();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                write(gen, lines);
            }
        });
    }

    /** Skips writes that a newer snapshot has already superseded. */
    private synchronized void write(long gen, List<String> lines) {
        if (gen < generation.get()) return;
        YamlConfiguration yml = new YamlConfiguration();
        yml.set(KEY, new ArrayList<String>(lines));
        try {
            yml.save(file);
        } catch (IOException e) {
            Logger.getLogger("TechnoBlocks").log(Level.WARNING, "Could not save " + file.getName(), e);
        }
    }

    public static final class Entry {
        private final BlockPos pos;
        private final String placed;
        private final String replacedState;

        Entry(BlockPos pos, String placed, String replacedState) {
            this.pos = pos;
            this.placed = placed;
            this.replacedState = replacedState;
        }

        public BlockPos getPos() {
            return pos;
        }

        public String getPlaced() {
            return placed;
        }

        public String getReplacedState() {
            return replacedState;
        }
    }
}
