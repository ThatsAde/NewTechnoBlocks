package it.technoade.technoblocks.modern;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModernAdapterTest {

    @Test
    void resetIsZero() {
        assertEquals(0f, ModernAdapter.stageToProgress(-1));
    }

    @Test
    void roundTripsThroughCraftBukkitFormula() {
        for (int s = 0; s <= 9; s++) {
            assertEquals(s, (int) (9 * ModernAdapter.stageToProgress(s)), "stage " + s);
        }
        assertTrue(ModernAdapter.stageToProgress(0) > 0f);
    }
}
