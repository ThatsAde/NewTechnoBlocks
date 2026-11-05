package it.technoade.technoblocks.modern;

import it.technoade.technoblocks.common.flat.FlatAdapter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 1.19.4+ (including 26.x): cracks through the Bukkit API, no NMS needed.
 */
public final class ModernAdapter extends FlatAdapter {

    public ModernAdapter(Plugin plugin) {
        super(plugin);
    }

    @Override
    public void sendCrack(Player viewer, int sourceId, Location location, int stage) {
        viewer.sendBlockDamage(location, stageToProgress(stage), sourceId);
    }

    /**
     * CraftBukkit maps progress to {@code (int) (9 * progress)} and treats 0 as reset,
     * so each stage is sent from the middle of its interval.
     */
    static float stageToProgress(int stage) {
        if (stage < 0) return 0f;
        return Math.min(1f, (stage + 0.5f) / 9f);
    }
}
