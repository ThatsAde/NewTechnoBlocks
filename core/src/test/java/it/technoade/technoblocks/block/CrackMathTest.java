package it.technoade.technoblocks.block;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CrackMathTest {

    @Test
    void stages() {
        assertEquals(0, CrackMath.stage(0, 10_000));
        assertEquals(0, CrackMath.stage(999, 10_000));
        assertEquals(1, CrackMath.stage(1_000, 10_000));
        assertEquals(9, CrackMath.stage(9_999, 10_000));
        assertEquals(9, CrackMath.stage(50_000, 10_000));
        assertEquals(0, CrackMath.stage(-5, 10_000));
    }

    @Test
    void zeroDuration() {
        assertEquals(9, CrackMath.stage(0, 0));
    }

    @Test
    void sendOnChangeOrRefresh() {
        assertTrue(CrackMath.shouldSend(3, 2, 1000, 900));
        assertFalse(CrackMath.shouldSend(3, 3, 5899, 900));
        assertTrue(CrackMath.shouldSend(3, 3, 5900, 900));
    }
}
