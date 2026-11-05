package it.technoade.technoblocks.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A creative break may still be cancelled by a later plugin; the block must only be forgotten
 * once the break is final (MONITOR, ignoring cancelled events).
 */
class CreativeBreakPriorityTest {

    @Test
    void creativeForgetRunsAtMonitorIgnoringCancelled() throws NoSuchMethodException {
        Method m = ProtectionListener.class.getMethod("onBreakFinal", BlockBreakEvent.class);
        EventHandler handler = m.getAnnotation(EventHandler.class);
        assertEquals(EventPriority.MONITOR, handler.priority());
        assertTrue(handler.ignoreCancelled());
    }
}
