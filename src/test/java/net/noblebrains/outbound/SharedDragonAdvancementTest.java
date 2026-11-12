package net.noblebrains.outbound;

import net.noblebrains.outbound.dragon.SharedDragonAdvancementHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SharedDragonAdvancementTest {

    @Test
    @DisplayName("Verify eligibility for shared dragon kill advancement requires damage AND being within radius in End")
    void testEligibilityConditions() {
        double radius = 200.0;

        // Meets all criteria: enabled, has damaged dragon, in End dimension, within radius
        assertTrue(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 0.0, 0.0, radius));
        assertTrue(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 100.0, 100.0, radius)); // dist ~141.4 <= 200
        assertTrue(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 200.0, 0.0, radius));   // exact boundary
        assertTrue(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 0.0, -200.0, radius));  // exact boundary

        // Outside radius (e.g. 200.1 or on outer End islands)
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 201.0, 0.0, radius));
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, true, 1000.0, 1000.0, radius));

        // Did NOT damage the dragon (e.g. AFK player) -> must NOT receive advancement
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, false, true, 0.0, 0.0, radius));
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, false, true, 50.0, 50.0, radius));

        // In another dimension (e.g. Overworld / Nether) -> must NOT receive advancement
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(true, true, false, 0.0, 0.0, radius));

        // Feature disabled in config -> must NOT receive advancement
        assertFalse(SharedDragonAdvancementHelper.isEligibleForAdvancement(false, true, true, 0.0, 0.0, radius));
    }
}
