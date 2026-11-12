package net.noblebrains.outbound;

import net.noblebrains.outbound.dragon.EnderDragonScalingHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EnderDragonScalingTest {

    @Test
    @DisplayName("Verify 26.4 Snapshot 8 Ender Dragon vertical flight acceleration fix")
    void testVerticalFlightAcceleration() {
        assertEquals(0.1D, EnderDragonScalingHelper.getVerticalFlightAcceleration(true, 0.01D), 1e-6);
        assertEquals(0.01D, EnderDragonScalingHelper.getVerticalFlightAcceleration(false, 0.01D), 1e-6);
    }

    @Test
    @DisplayName("Verify Option A diminishing returns damage multiplier by player count")
    void testDamageMultiplierOptionA() {
        double k = 0.25;

        // 0 or 1 player: 100% damage (0% resistance)
        assertEquals(1.0F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 0, k), 1e-4F);
        assertEquals(1.0F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 1, k), 1e-4F);

        // 2 players: 1 / 1.25 = 0.80 (20% resistance)
        assertEquals(0.80F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 2, k), 1e-4F);

        // 3 players: 1 / 1.50 = 0.6667 (33.3% resistance)
        assertEquals(2.0F / 3.0F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 3, k), 1e-4F);

        // 4 players: 1 / 1.75 = 0.5714 (42.9% resistance)
        assertEquals(1.0F / 1.75F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 4, k), 1e-4F);

        // 5 players: 1 / 2.00 = 0.50 (50% resistance)
        assertEquals(0.50F, EnderDragonScalingHelper.calculateDamageMultiplier(true, 5, k), 1e-4F);

        // Disabled feature: always 1.0
        assertEquals(1.0F, EnderDragonScalingHelper.calculateDamageMultiplier(false, 5, k), 1e-4F);
    }

    @Test
    @DisplayName("Verify incoming damage scaling")
    void testScaleIncomingDamage() {
        float baseDamage = 20.0F;
        double k = 0.25;

        assertEquals(20.0F, EnderDragonScalingHelper.scaleIncomingDamage(baseDamage, true, 1, k), 1e-3F);
        assertEquals(16.0F, EnderDragonScalingHelper.scaleIncomingDamage(baseDamage, true, 2, k), 1e-3F);
        assertEquals(10.0F, EnderDragonScalingHelper.scaleIncomingDamage(baseDamage, true, 5, k), 1e-3F);
    }
}
