package it.technoade.technoblocks.common.flat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * "NAME:data" goes through Bukkit's legacy conversion, which future servers may drop.
 * Without a server it is unavailable too: parsing must return null instead of throwing.
 */
class FlatLegacyNameTest {

    @Test
    void legacyNameWithoutLegacySupportReturnsNull() {
        assertNull(FlatAdapter.parseName("WOOL:14"));
    }
}
