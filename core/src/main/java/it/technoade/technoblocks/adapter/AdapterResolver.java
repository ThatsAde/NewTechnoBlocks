package it.technoade.technoblocks.adapter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Picks the adapter class for the running server.
 */
public final class AdapterResolver {

    public static final String MODERN = "it.technoade.technoblocks.modern.ModernAdapter";

    private static final Set<String> NMS_REVISIONS = new HashSet<String>(Arrays.asList(
            "v1_8_R3", "v1_9_R2", "v1_10_R1", "v1_11_R1", "v1_12_R1",
            "v1_13_R2", "v1_14_R1", "v1_15_R1", "v1_16_R3",
            "v1_17_R1", "v1_18_R2", "v1_19_R1", "v1_19_R2"));

    private AdapterResolver() {
    }

    /**
     * @param hasSourceIdDamageApi whether Player#sendBlockDamage(Location, float, int) exists
     * @param craftBukkitPackage   package of the CraftServer class
     * @return adapter class name, or null if the server is not supported
     */
    public static String resolve(boolean hasSourceIdDamageApi, String craftBukkitPackage) {
        if (hasSourceIdDamageApi) return MODERN;
        if (craftBukkitPackage == null) return null;
        String revision = craftBukkitPackage.substring(craftBukkitPackage.lastIndexOf('.') + 1);
        if (!revision.startsWith("v") || !NMS_REVISIONS.contains(revision)) return null;
        return "it.technoade.technoblocks.nms." + revision + ".AdapterImpl";
    }
}
