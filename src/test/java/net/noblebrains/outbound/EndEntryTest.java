package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.end.EndEntryHelper;
import net.noblebrains.outbound.end.EnderEyeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EndEntryTest {

    private static final ResourceKey<Level> OVERWORLD = ResourceKey.create(
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dimension")),
            Identifier.withDefaultNamespace("overworld")
    );
    private static final ResourceKey<Level> NETHER = ResourceKey.create(
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dimension")),
            Identifier.withDefaultNamespace("the_nether")
    );
    private static final ResourceKey<Level> THE_END = ResourceKey.create(
            ResourceKey.createRegistryKey(Identifier.withDefaultNamespace("dimension")),
            Identifier.withDefaultNamespace("the_end")
    );

    @BeforeEach
    void setUp() {
        OutboundConfig.get().allowEndEntry = true;
        OutboundConfig.get().allowEnderEyeTracking = true;
    }

    @Test
    @DisplayName("Verify End entry and Ender Eye tracking allowed by default")
    void testEndEntryAllowedByDefault() {
        assertTrue(EndEntryHelper.isEndEntryAllowed());
        assertTrue(EnderEyeHelper.isEnderEyeTrackingAllowed());
        assertTrue(EndEntryHelper.canTransitionDimension(OVERWORLD, THE_END));
        assertTrue(EndEntryHelper.canTransitionDimension(NETHER, THE_END));
    }

    @Test
    @DisplayName("Verify End entry blocked when allowEndEntry is false")
    void testEndEntryBlockedWhenDisabled() {
        OutboundConfig.get().allowEndEntry = false;
        assertFalse(EndEntryHelper.isEndEntryAllowed());
        assertFalse(EndEntryHelper.canTransitionDimension(OVERWORLD, THE_END));
        assertFalse(EndEntryHelper.canTransitionDimension(NETHER, THE_END));
    }

    @Test
    @DisplayName("Verify leaving the End is always allowed even when End entry is disabled")
    void testLeavingEndAlwaysAllowed() {
        OutboundConfig.get().allowEndEntry = false;
        assertTrue(EndEntryHelper.canTransitionDimension(THE_END, OVERWORLD), "Leaving End to Overworld must be allowed");
        assertTrue(EndEntryHelper.canTransitionDimension(THE_END, NETHER), "Leaving End to Nether must be allowed");
        assertTrue(EndEntryHelper.canTransitionDimension(THE_END, THE_END), "Intra-End travel must be allowed");
    }

    @Test
    @DisplayName("Verify regular dimensions inter-travel is never affected")
    void testOtherDimensionsUnaffected() {
        OutboundConfig.get().allowEndEntry = false;
        assertTrue(EndEntryHelper.canTransitionDimension(OVERWORLD, NETHER));
        assertTrue(EndEntryHelper.canTransitionDimension(NETHER, OVERWORLD));
        assertTrue(EndEntryHelper.canTransitionDimension(OVERWORLD, OVERWORLD));
    }

    @Test
    @DisplayName("Verify showEndClosedMessage popup is disabled by default")
    void testShowEndClosedMessageDefaultDisabled() {
        assertFalse(OutboundConfig.get().showEndClosedMessage, "Popup message should be disabled by default");
    }

    @Test
    @DisplayName("Verify Ender Eye tracking can be disabled independently from End entry")
    void testIndependentEyeTrackingToggle() {
        // End entry true, eye tracking false
        OutboundConfig.get().allowEndEntry = true;
        OutboundConfig.get().allowEnderEyeTracking = false;
        assertTrue(EndEntryHelper.isEndEntryAllowed());
        assertFalse(EnderEyeHelper.isEnderEyeTrackingAllowed());

        // End entry false, eye tracking true
        OutboundConfig.get().allowEndEntry = false;
        OutboundConfig.get().allowEnderEyeTracking = true;
        assertFalse(EndEntryHelper.isEndEntryAllowed());
        assertTrue(EnderEyeHelper.isEnderEyeTrackingAllowed());
    }

    @Test
    @DisplayName("Verify calculateRandomTarget computes expected coordinates in all quadrants")
    void testCalculateRandomTargetCoordinates() {
        BlockPos origin = new BlockPos(100, 64, 200);
        double distance = 500.0;

        // Angle 0 (East / +X)
        BlockPos targetEast = EnderEyeHelper.calculateRandomTarget(origin, 0.0, distance);
        assertEquals(600, targetEast.getX());
        assertEquals(64, targetEast.getY());
        assertEquals(200, targetEast.getZ());

        // Angle PI/2 (South / +Z)
        BlockPos targetSouth = EnderEyeHelper.calculateRandomTarget(origin, Math.PI / 2.0, distance);
        assertEquals(100, targetSouth.getX());
        assertEquals(64, targetSouth.getY());
        assertEquals(700, targetSouth.getZ());

        // Angle PI (West / -X)
        BlockPos targetWest = EnderEyeHelper.calculateRandomTarget(origin, Math.PI, distance);
        assertEquals(-400, targetWest.getX());
        assertEquals(64, targetWest.getY());
        assertEquals(200, targetWest.getZ());

        // Angle 3*PI/2 (North / -Z)
        BlockPos targetNorth = EnderEyeHelper.calculateRandomTarget(origin, 3.0 * Math.PI / 2.0, distance);
        assertEquals(100, targetNorth.getX());
        assertEquals(64, targetNorth.getY());
        assertEquals(-300, targetNorth.getZ());
    }
}
