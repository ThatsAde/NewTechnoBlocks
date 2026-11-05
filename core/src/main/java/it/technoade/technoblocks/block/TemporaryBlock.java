package it.technoade.technoblocks.block;

import it.technoade.technoblocks.api.MaterialSpec;

/**
 * A placed block that will be restored to {@link #getReplacedState()} when it expires.
 */
public final class TemporaryBlock {

    private final BlockPos pos;
    private final MaterialSpec placed;
    private final String replacedState;
    private final long placedAt;
    private final long expiresAt;
    private final int sourceId;
    private final long sequence;
    private int lastSentStage = -1;
    private long lastSentAt;

    TemporaryBlock(BlockPos pos, MaterialSpec placed, String replacedState, long placedAt, long expiresAt,
                   int sourceId, long sequence) {
        this.pos = pos;
        this.placed = placed;
        this.replacedState = replacedState;
        this.placedAt = placedAt;
        this.expiresAt = expiresAt;
        this.sourceId = sourceId;
        this.sequence = sequence;
    }

    public BlockPos getPos() {
        return pos;
    }

    public MaterialSpec getPlaced() {
        return placed;
    }

    public String getReplacedState() {
        return replacedState;
    }

    public long getPlacedAt() {
        return placedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public int getSourceId() {
        return sourceId;
    }

    long getSequence() {
        return sequence;
    }

    public int getLastSentStage() {
        return lastSentStage;
    }

    public long getLastSentAt() {
        return lastSentAt;
    }

    public void markSent(int stage, long at) {
        this.lastSentStage = stage;
        this.lastSentAt = at;
    }
}
