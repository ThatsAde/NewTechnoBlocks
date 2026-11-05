package it.technoade.technoblocks.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemProtectionTest {

    @Test
    void tags() {
        assertTrue(ItemProtection.isPluginTag("selector"));
        assertTrue(ItemProtection.isPluginTag("block:stone"));
        assertFalse(ItemProtection.isPluginTag(null));
        assertFalse(ItemProtection.isPluginTag("other"));
        assertEquals("stone", ItemProtection.blockKey("block:stone"));
        assertNull(ItemProtection.blockKey("selector"));
        assertNull(ItemProtection.blockKey(null));
    }

    @Test
    void hotbarNumberKeySwapIsCancelled() {
        assertTrue(ItemProtection.shouldCancelClick(null, null, "block:stone"));
    }

    @Test
    void cursorOrCurrent() {
        assertTrue(ItemProtection.shouldCancelClick("selector", null, null));
        assertTrue(ItemProtection.shouldCancelClick(null, "block:x", null));
    }

    @Test
    void foreignItemsFree() {
        assertFalse(ItemProtection.shouldCancelClick(null, "other", null));
        assertFalse(ItemProtection.shouldCancelClick(null, null, null));
    }
}
