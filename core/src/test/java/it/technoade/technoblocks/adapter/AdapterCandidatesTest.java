package it.technoade.technoblocks.adapter;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Paper may expose the source-id damage API on versions whose Java can't load the Java 17 modern
 * module (1.17.1 runs Java 16), so the NMS adapter is the fallback.
 */
class AdapterCandidatesTest {

    @Test
    void modernThenNmsFallback() {
        assertEquals(Arrays.asList(AdapterResolver.MODERN, "it.technoade.technoblocks.nms.v1_17_R1.AdapterImpl"),
                AdapterResolver.candidates(true, "org.bukkit.craftbukkit.v1_17_R1"));
    }

    @Test
    void modernOnlyWhenNoRevision() {
        assertEquals(Collections.singletonList(AdapterResolver.MODERN),
                AdapterResolver.candidates(true, "org.bukkit.craftbukkit"));
    }

    @Test
    void nmsOnlyWithoutApi() {
        assertEquals(Collections.singletonList("it.technoade.technoblocks.nms.v1_8_R3.AdapterImpl"),
                AdapterResolver.candidates(false, "org.bukkit.craftbukkit.v1_8_R3"));
    }

    @Test
    void emptyWhenUnsupported() {
        assertEquals(Collections.emptyList(), AdapterResolver.candidates(false, "org.bukkit.craftbukkit.v1_7_R4"));
    }
}
