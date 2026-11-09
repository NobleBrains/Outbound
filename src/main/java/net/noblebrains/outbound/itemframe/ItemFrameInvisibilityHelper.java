package net.noblebrains.outbound.itemframe;

import net.noblebrains.outbound.config.OutboundConfig;

public final class ItemFrameInvisibilityHelper {

    private ItemFrameInvisibilityHelper() {
    }

    public static boolean isFeatureEnabled() {
        return OutboundConfig.get().enableInvisibleItemFrames;
    }

    public static boolean canTurnInvisibleOnShiftInteract(
            boolean featureEnabled,
            boolean isFixed,
            boolean hasItemInFrame,
            boolean isCurrentlyInvisible,
            boolean isSecondaryUseActive
    ) {
        return featureEnabled
                && !isFixed
                && hasItemInFrame
                && !isCurrentlyInvisible
                && isSecondaryUseActive;
    }

    public static boolean shouldResetInvisibilityOnItemChange(
            boolean hasItemInFrame,
            boolean isCurrentlyInvisible
    ) {
        return !hasItemInFrame && isCurrentlyInvisible;
    }
}
