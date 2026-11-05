package it.technoade.technoblocks.player;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Plugin items must not drop on death. */
class DeathDropsTest {

    @Test
    void removesOnlyPluginItems() {
        List<String> drops = new ArrayList<String>(Arrays.asList("selector", "sword", "block:stone", "dirt"));
        int removed = ItemProtection.removeTagged(drops, new Function<String, String>() {
            @Override
            public String apply(String item) {
                return item;
            }
        });
        assertEquals(2, removed);
        assertEquals(Arrays.asList("sword", "dirt"), drops);
    }
}
