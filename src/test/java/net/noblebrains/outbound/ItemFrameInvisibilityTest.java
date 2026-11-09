package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.itemframe.ItemFrameInvisibilityHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ItemFrameInvisibilityTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableInvisibleItemFrames = true;
    }

    @Test
    @DisplayName("Verify invisible item frames feature is enabled by default")
    void testDefaultConfig() {
        assertTrue(ItemFrameInvisibilityHelper.isFeatureEnabled());
    }

    @Test
    @DisplayName("Verify Shift+Right-Click turns Item Frame invisible only when it contains an item")
    void testShiftRightClickConditions() {
        // Feature enabled, not fixed, has item, currently visible, sneaking -> turns invisible
        assertTrue(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(true, false, true, false, true));

        // Empty item frame -> cannot turn invisible
        assertFalse(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(true, false, false, false, true));

        // Not sneaking (normal right-click) -> normal item rotation, does not trigger invisibility
        assertFalse(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(true, false, true, false, false));

        // Already invisible -> does not re-trigger
        assertFalse(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(true, false, true, true, true));

        // Fixed item frame -> cannot be modified
        assertFalse(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(true, true, true, false, true));

        // Feature disabled in config -> does not trigger
        assertFalse(ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(false, false, true, false, true));
    }

    @Test
    @DisplayName("Verify Item Frame automatically becomes visible again when item is removed")
    void testResetInvisibilityWhenItemRemoved() {
        // Invisible frame with item removed -> resets to visible
        assertTrue(ItemFrameInvisibilityHelper.shouldResetInvisibilityOnItemChange(false, true));

        // Invisible frame that still has an item -> stays invisible
        assertFalse(ItemFrameInvisibilityHelper.shouldResetInvisibilityOnItemChange(true, true));

        // Already visible frame -> no reset needed
        assertFalse(ItemFrameInvisibilityHelper.shouldResetInvisibilityOnItemChange(false, false));
    }
}
