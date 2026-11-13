package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.shulker.ShulkerPickItemHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShulkerPickItemTest {

    @Test
    void allowsPlacementWhenHotbarSlotIsEmpty() {
        assertTrue(ShulkerPickItemHelper.canPlaceExtractedItem(true, -1));
        assertTrue(ShulkerPickItemHelper.canPlaceExtractedItem(true, 9));
    }

    @Test
    void allowsPlacementWhenHotbarIsFullAndMainInventoryHasFreeSlot() {
        assertTrue(ShulkerPickItemHelper.canPlaceExtractedItem(false, 9));
        assertTrue(ShulkerPickItemHelper.canPlaceExtractedItem(false, 35));
    }

    @Test
    void abortsSafelyWhenHotbarAndMainInventoryAreFull() {
        assertFalse(ShulkerPickItemHelper.canPlaceExtractedItem(false, -1));
        assertFalse(ShulkerPickItemHelper.canPlaceExtractedItem(false, 8));
    }

    @Test
    void shulkerBoxPickBlockEnabledByDefault() {
        assertTrue(OutboundConfig.get().enableShulkerBoxPickBlock);
    }
}
