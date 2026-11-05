package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * Active temporary blocks, indexed by position and ordered by expiry.
 * Replaced or removed entries stay in the queue and are skipped when polled.
 */
public final class TemporaryBlockRegistry {

    private final Map<BlockPos, TemporaryBlock> byPos = new HashMap<BlockPos, TemporaryBlock>();
    private final PriorityQueue<TemporaryBlock> byExpiry = new PriorityQueue<TemporaryBlock>(16,
            new Comparator<TemporaryBlock>() {
                @Override
                public int compare(TemporaryBlock a, TemporaryBlock b) {
                    int c = Long.compare(a.getExpiresAt(), b.getExpiresAt());
                    return c != 0 ? c : Long.compare(a.getSequence(), b.getSequence());
                }
            });

    /** Counts down from the top of the int range so ids never clash with real entity ids. */
    private int nextSourceId = Integer.MAX_VALUE;
    private long nextSequence;

    /** Registers a block, replacing any entry at the same position. */
    public TemporaryBlock add(BlockPos pos, MaterialSpec placed, String replacedState, long now, long durationMillis) {
        TemporaryBlock block = new TemporaryBlock(pos, placed, replacedState, now, now + durationMillis,
                nextSourceId--, nextSequence++);
        if (nextSourceId < 1_000_000_000) nextSourceId = Integer.MAX_VALUE;
        byPos.put(pos, block);
        byExpiry.add(block);
        return block;
    }

    public TemporaryBlock get(BlockPos pos) {
        return byPos.get(pos);
    }

    public TemporaryBlock remove(BlockPos pos) {
        return byPos.remove(pos);
    }

    /** Removes and returns the expired blocks, earliest first. */
    public List<TemporaryBlock> pollExpired(long now) {
        List<TemporaryBlock> expired = new ArrayList<TemporaryBlock>();
        while (!byExpiry.isEmpty() && byExpiry.peek().getExpiresAt() <= now) {
            TemporaryBlock block = byExpiry.poll();
            if (byPos.get(block.getPos()) == block) {
                byPos.remove(block.getPos());
                expired.add(block);
            }
        }
        return expired;
    }

    public List<TemporaryBlock> drainAll() {
        List<TemporaryBlock> all = new ArrayList<TemporaryBlock>(byPos.values());
        byPos.clear();
        byExpiry.clear();
        return all;
    }

    /** Removes and returns every block in the given world. */
    public List<TemporaryBlock> drainWorld(String world) {
        List<TemporaryBlock> drained = new ArrayList<TemporaryBlock>();
        java.util.Iterator<TemporaryBlock> it = byPos.values().iterator();
        while (it.hasNext()) {
            TemporaryBlock block = it.next();
            if (block.getPos().getWorld().equals(world)) {
                drained.add(block);
                it.remove();
            }
        }
        return drained;
    }

    public Collection<TemporaryBlock> all() {
        return Collections.unmodifiableCollection(byPos.values());
    }

    public int size() {
        return byPos.size();
    }
}
