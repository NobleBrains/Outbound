package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.leash.PlayerLeashHelper;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerLeashTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enablePlayerLeashing = true;
        OutboundConfig.get().playerLeashDistanceMin = 4.0;
        OutboundConfig.get().playerLeashDistanceMax = 10.0;
        OutboundConfig.get().allowLeashedRemoveFenceKnot = false;
    }

    @Test
    @DisplayName("Verify player leashing defaults")
    void testDefaultConfig() {
        assertTrue(OutboundConfig.get().enablePlayerLeashing);
        assertEquals(4.0, OutboundConfig.get().playerLeashDistanceMin);
        assertEquals(10.0, OutboundConfig.get().playerLeashDistanceMax);
        assertFalse(OutboundConfig.get().allowLeashedRemoveFenceKnot);
    }

    @Test
    @DisplayName("Alive non-spectator player can be leashed when feature is enabled")
    void testCanPlayerBeLeashed() {
        assertTrue(PlayerLeashHelper.canPlayerBeLeashed(true, true, false));
        assertFalse(PlayerLeashHelper.canPlayerBeLeashed(false, true, false));
        assertFalse(PlayerLeashHelper.canPlayerBeLeashed(true, false, false));
        assertFalse(PlayerLeashHelper.canPlayerBeLeashed(true, true, true));
    }

    @Test
    @DisplayName("Grace period of 20 ticks prevents immediate off-hand detach on right-click")
    void testDetachGracePeriod() {
        // Same tick (e.g., off-hand packet immediately following main-hand packet)
        assertFalse(PlayerLeashHelper.canDetachAfterGracePeriod(100, 100));
        // Within 20 ticks
        assertFalse(PlayerLeashHelper.canDetachAfterGracePeriod(100, 120));
        // After 20 ticks
        assertTrue(PlayerLeashHelper.canDetachAfterGracePeriod(100, 121));
    }

    @Test
    @DisplayName("Neck position adjusts for pitch and sneaking")
    void testCalculateNeckPosition() {
        Vec3 base = new Vec3(10.0, 64.0, 20.0);
        assertEquals(new Vec3(10.0, 65.3, 19.85), PlayerLeashHelper.calculateNeckPosition(base, 0.0F, false));
        assertEquals(new Vec3(10.0, 64.8, 19.85), PlayerLeashHelper.calculateNeckPosition(base, 0.0F, true));
        assertEquals(new Vec3(10.0, 65.9, 19.85), PlayerLeashHelper.calculateNeckPosition(base, 45.0F, false));
    }

    @Test
    @DisplayName("Pull vector points from leashed player towards holder")
    void testCalculatePullVector() {
        Vec3 playerPos = new Vec3(0.0, 64.0, 0.0);
        Vec3 holderPos = new Vec3(5.0, 64.0, 0.0);
        Vec3 pull = PlayerLeashHelper.calculatePullVector(playerPos, holderPos, 5.0);
        assertEquals(0.4, pull.x, 1e-6);
        assertEquals(0.0, pull.y, 1e-6);
        assertEquals(0.0, pull.z, 1e-6);
    }
}
