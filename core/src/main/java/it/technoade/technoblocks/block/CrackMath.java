package it.technoade.technoblocks.block;

/**
 * Crack stage progression.
 */
public final class CrackMath {

    /** Clients drop a crack animation after ~20 s without updates, so resend well before that. */
    public static final long REFRESH_MILLIS = 5000L;

    private CrackMath() {
    }

    /** @return stage 0..9 growing linearly over the duration */
    public static int stage(long elapsedMillis, long durationMillis) {
        if (durationMillis <= 0) return 9;
        if (elapsedMillis <= 0) return 0;
        long stage = elapsedMillis * 10 / durationMillis;
        return (int) Math.min(9, stage);
    }

    public static boolean shouldSend(int stage, int lastSentStage, long now, long lastSentAt) {
        return stage != lastSentStage || now - lastSentAt >= REFRESH_MILLIS;
    }
}
