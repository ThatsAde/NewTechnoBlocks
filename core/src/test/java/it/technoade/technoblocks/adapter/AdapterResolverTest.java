package it.technoade.technoblocks.adapter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class AdapterResolverTest {

    @Test
    void modernWhenApiPresent() {
        assertEquals(AdapterResolver.MODERN, AdapterResolver.resolve(true, "org.bukkit.craftbukkit"));
        assertEquals(AdapterResolver.MODERN, AdapterResolver.resolve(true, "org.bukkit.craftbukkit.v1_20_R3"));
    }

    @Test
    void legacyRevisionFromPackage() {
        assertEquals("it.technoade.technoblocks.nms.v1_8_R3.AdapterImpl",
                AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_8_R3"));
        assertEquals("it.technoade.technoblocks.nms.v1_19_R2.AdapterImpl",
                AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_19_R2"));
    }

    @Test
    void unsupportedRevision() {
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_7_R4"));
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit.v1_16_R1"));
        assertNull(AdapterResolver.resolve(false, "org.bukkit.craftbukkit"));
    }
}
