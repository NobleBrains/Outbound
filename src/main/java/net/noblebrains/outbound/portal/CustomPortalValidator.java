package net.noblebrains.outbound.portal;

import net.noblebrains.outbound.config.OutboundConfig;

public class CustomPortalValidator {

    /**
     * Determines whether the given portal dimensions are valid.
     * Supports:
     * - 1x1
     * - 1x2 (vertical)
     * - 2x2
     * - Standard vanilla portals (width >= 2 and height >= 3 up to 21x21)
     * - Arbitrary custom sizes up to 21x21 if allowAnyCustomPortalSize is enabled.
     */
    public static boolean isAllowedSize(int width, int height) {
        if (!OutboundConfig.get().enableCustomPortals) {
            return width >= 2 && width <= 21 && height >= 3 && height <= 21;
        }

        if (width < 1 || height < 1 || width > 21 || height > 21) {
            return false;
        }

        // Standard vanilla portal (2x3 to 21x21)
        if (width >= 2 && height >= 3) {
            return true;
        }

        // 1x1 portal
        if (width == 1 && height == 1) {
            return true;
        }

        // 1x2 (vertical) portal
        if (width == 1 && height == 2) {
            return true;
        }

        // 2x2 portal
        if (width == 2 && height == 2) {
            return true;
        }

        // Optional toggle for any custom portal size (e.g. 1x3, 1x4)
        return OutboundConfig.get().allowAnyCustomPortalSize;
    }
}
