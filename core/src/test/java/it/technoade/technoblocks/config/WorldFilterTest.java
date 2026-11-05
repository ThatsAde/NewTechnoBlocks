package it.technoade.technoblocks.config;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorldFilterTest {

    @Test
    void whitelist() {
        WorldFilter f = new WorldFilter(true, new HashSet<String>(Arrays.asList("hub")));
        assertTrue(f.isEnabled("hub"));
        assertFalse(f.isEnabled("survival"));
    }

    @Test
    void blacklist() {
        WorldFilter f = new WorldFilter(false, new HashSet<String>(Arrays.asList("survival")));
        assertTrue(f.isEnabled("hub"));
        assertFalse(f.isEnabled("survival"));
    }

    @Test
    void caseInsensitive() {
        WorldFilter f = new WorldFilter(true, new HashSet<String>(Arrays.asList("Hub")));
        assertTrue(f.isEnabled("HUB"));
    }
}
