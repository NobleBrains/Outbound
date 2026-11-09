package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.portal.CustomPortalValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PortalTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableCustomPortals = true;
        OutboundConfig.get().allowAnyCustomPortalSize = false;
    }

    @Test
    @DisplayName("Verify 1x1, 1x2 vertical, and 2x2 Nether portals are valid")
    void testCustomAllowedPortals() {
        assertTrue(CustomPortalValidator.isAllowedSize(1, 1), "1x1 portal should be valid");
        assertTrue(CustomPortalValidator.isAllowedSize(1, 2), "1x2 vertical portal should be valid");
        assertTrue(CustomPortalValidator.isAllowedSize(2, 2), "2x2 portal should be valid");
    }

    @Test
    @DisplayName("Verify standard vanilla portal dimensions remain valid")
    void testVanillaPortals() {
        assertTrue(CustomPortalValidator.isAllowedSize(2, 3), "Standard 2x3 portal should be valid");
        assertTrue(CustomPortalValidator.isAllowedSize(3, 4), "3x4 portal should be valid");
        assertTrue(CustomPortalValidator.isAllowedSize(4, 5), "4x5 portal should be valid");
        assertTrue(CustomPortalValidator.isAllowedSize(21, 21), "Max 21x21 portal should be valid");
    }

    @Test
    @DisplayName("Verify non-whitelisted dimensions are rejected by default")
    void testDisallowedDimensions() {
        assertFalse(CustomPortalValidator.isAllowedSize(2, 1), "2x1 horizontal portal should be invalid by default");
        assertFalse(CustomPortalValidator.isAllowedSize(1, 3), "1x3 portal should be invalid by default");
        assertFalse(CustomPortalValidator.isAllowedSize(0, 0), "0x0 should be invalid");
        assertFalse(CustomPortalValidator.isAllowedSize(22, 22), "Exceeding max size should be invalid");
        assertFalse(CustomPortalValidator.isAllowedSize(-1, 2), "Negative dimensions should be invalid");
    }

    @Test
    @DisplayName("Verify allowAnyCustomPortalSize config allows arbitrary sizes")
    void testAllowAnyCustomSize() {
        OutboundConfig.get().allowAnyCustomPortalSize = true;
        assertTrue(CustomPortalValidator.isAllowedSize(1, 3), "1x3 should be valid when allowAnyCustomPortalSize is true");
        assertTrue(CustomPortalValidator.isAllowedSize(2, 1), "2x1 should be valid when allowAnyCustomPortalSize is true");
        assertFalse(CustomPortalValidator.isAllowedSize(22, 22), "Still capped by max bounds");
    }

    @Test
    @DisplayName("Verify disabling custom portals enforces vanilla constraints")
    void testDisableCustomPortals() {
        OutboundConfig.get().enableCustomPortals = false;
        assertFalse(CustomPortalValidator.isAllowedSize(1, 1), "1x1 should be invalid when custom portals disabled");
        assertFalse(CustomPortalValidator.isAllowedSize(1, 2), "1x2 should be invalid when custom portals disabled");
        assertFalse(CustomPortalValidator.isAllowedSize(2, 2), "2x2 should be invalid when custom portals disabled");
        assertTrue(CustomPortalValidator.isAllowedSize(2, 3), "Standard 2x3 must still be valid");
    }
}
