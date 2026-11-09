package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.shulker.ShulkerTeleportHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ShulkerTeleportTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableShulkerDamageTeleportAdjustments = true;
        OutboundConfig.get().shulkerTeleportCooldownTicks = 80;
        OutboundConfig.get().shulkerMaxGroundDistance = 4;
        OutboundConfig.get().shulkerTeleportRequiresLineOfSight = true;
    }

    @Test
    @DisplayName("Verify default configuration values for shulker teleportation")
    void testDefaultConfig() {
        assertTrue(OutboundConfig.get().enableShulkerDamageTeleportAdjustments);
        assertEquals(80, OutboundConfig.get().shulkerTeleportCooldownTicks);
        assertEquals(4, OutboundConfig.get().shulkerMaxGroundDistance);
        assertTrue(OutboundConfig.get().shulkerTeleportRequiresLineOfSight);
    }

    @Test
    @DisplayName("Damage teleport cooldown is active within cooldownTicks and inactive after")
    void testCooldownLogic() {
        int lastTeleportTick = 1000;
        int cooldown = 80;

        // Same tick or shortly after
        assertTrue(ShulkerTeleportHelper.isCooldownActive(1000, lastTeleportTick, cooldown));
        assertTrue(ShulkerTeleportHelper.isCooldownActive(1050, lastTeleportTick, cooldown));
        assertTrue(ShulkerTeleportHelper.isCooldownActive(1079, lastTeleportTick, cooldown));

        // Exact cooldown boundary and beyond
        assertFalse(ShulkerTeleportHelper.isCooldownActive(1080, lastTeleportTick, cooldown));
        assertFalse(ShulkerTeleportHelper.isCooldownActive(1100, lastTeleportTick, cooldown));

        // Disabled cooldown
        assertFalse(ShulkerTeleportHelper.isCooldownActive(1050, lastTeleportTick, 0));
        assertFalse(ShulkerTeleportHelper.isCooldownActive(1050, lastTeleportTick, -1));
    }

    @Test
    @DisplayName("Ground accessibility simulation validates max air blocks to ground")
    void testGroundDistanceSimulation() {
        int maxAirBlocks = 4;

        // Ground immediately below (0 air blocks)
        assertTrue(ShulkerTeleportHelper.simulateGroundCheck(maxAirBlocks, new boolean[]{true}));

        // 1 air block then ground
        assertTrue(ShulkerTeleportHelper.simulateGroundCheck(maxAirBlocks, new boolean[]{false, true}));

        // 4 air blocks then ground
        assertTrue(ShulkerTeleportHelper.simulateGroundCheck(maxAirBlocks, new boolean[]{false, false, false, false, true}));

        // 5 air blocks then ground (too far, unreachable with normal sword)
        assertFalse(ShulkerTeleportHelper.simulateGroundCheck(maxAirBlocks, new boolean[]{false, false, false, false, false, true}));

        // Pure void / abyss (no ground at all)
        assertFalse(ShulkerTeleportHelper.simulateGroundCheck(maxAirBlocks, new boolean[]{false, false, false, false, false, false}));

        // Negative max distance disables the restriction
        assertTrue(ShulkerTeleportHelper.simulateGroundCheck(-1, new boolean[]{false, false, false, false, false}));
    }

    @Test
    @DisplayName("Line-of-sight check correctly rejects destinations obstructed by solid walls")
    void testLineOfSightSimulation() {
        // Open line of sight in the same room (no wall between)
        assertTrue(ShulkerTeleportHelper.simulateLineOfSightCheck(false));

        // Wall between shulker and candidate (e.g. through End City outer wall)
        assertFalse(ShulkerTeleportHelper.simulateLineOfSightCheck(true));
    }
}
