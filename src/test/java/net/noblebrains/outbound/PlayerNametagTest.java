package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerNametagTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enablePlayerNametagRenaming = true;
    }

    @Test
    @DisplayName("Verify player nametag renaming is enabled by default")
    void testDefaultConfig() {
        assertTrue(PlayerNametagHelper.isFeatureEnabled());
    }

    @Test
    @DisplayName("Verify interaction preconditions for renaming another player")
    void testCanRenamePlayerPreconditions() {
        // Normal player renaming another alive non-spectator player
        assertTrue(PlayerNametagHelper.canRenamePlayer(true, true, false, false, false));

        // Feature disabled -> blocked
        assertFalse(PlayerNametagHelper.canRenamePlayer(false, true, false, false, false));

        // Dead target -> blocked
        assertFalse(PlayerNametagHelper.canRenamePlayer(true, false, false, false, false));

        // Target is spectator -> blocked
        assertFalse(PlayerNametagHelper.canRenamePlayer(true, true, true, false, false));

        // Actor is spectator -> blocked
        assertFalse(PlayerNametagHelper.canRenamePlayer(true, true, false, true, false));

        // Renaming self -> blocked
        assertFalse(PlayerNametagHelper.canRenamePlayer(true, true, false, false, true));
    }

    @Test
    @DisplayName("Verify renaming and resetting back to original name via Name Tag")
    void testRenameAndResetToOriginalName() {
        String originalName = "PlayerOne";

        // Renaming to a new custom name
        assertTrue(PlayerNametagHelper.wouldChangePlayerName(originalName, null, "Bürgermeister"));
        assertEquals("Bürgermeister", PlayerNametagHelper.resolveTargetCustomName(originalName, "Bürgermeister"));

        // Re-applying the exact same custom name does not waste a nametag
        assertFalse(PlayerNametagHelper.wouldChangePlayerName(originalName, "Bürgermeister", "Bürgermeister"));

        // Renaming to a second custom name
        assertTrue(PlayerNametagHelper.wouldChangePlayerName(originalName, "Bürgermeister", "Kumpel"));
        assertEquals("Kumpel", PlayerNametagHelper.resolveTargetCustomName(originalName, "Kumpel"));

        // Resetting back to the original name clears the custom name (returns null)
        assertTrue(PlayerNametagHelper.wouldChangePlayerName(originalName, "Kumpel", "PlayerOne"));
        assertNull(PlayerNametagHelper.resolveTargetCustomName(originalName, "PlayerOne"));

        // Using original name on an un-renamed player does not waste a nametag
        assertFalse(PlayerNametagHelper.wouldChangePlayerName(originalName, null, "PlayerOne"));
    }

    @Test
    @DisplayName("Verify effective name respects toggle and falls back to original name")
    void testEffectivePlayerName() {
        assertEquals("Kumpel", PlayerNametagHelper.getEffectivePlayerName("PlayerOne", "Kumpel", true));
        assertEquals("PlayerOne", PlayerNametagHelper.getEffectivePlayerName("PlayerOne", "Kumpel", false));
        assertEquals("PlayerOne", PlayerNametagHelper.getEffectivePlayerName("PlayerOne", null, true));
    }

    @Test
    @DisplayName("Verify profile packet name truncates safely at 16 characters")
    void testProfilePacketNameLengthLimit() {
        assertEquals("ShortName", PlayerNametagHelper.getProfilePacketName("ShortName"));
        assertEquals("Exactly16Chars!!", PlayerNametagHelper.getProfilePacketName("Exactly16Chars!!"));
        assertEquals("1234567890123456", PlayerNametagHelper.getProfilePacketName("12345678901234567890"));
    }
}
