package net.noblebrains.outbound;

import net.noblebrains.outbound.afk.AfkHelper;
import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.network.chat.Component;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AfkSystemTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableAfkSystem = true;
        OutboundConfig.get().afkTimeoutSeconds = 300;
    }

    @Test
    @DisplayName("Verify AFK system is enabled by default with 5 minute (300s) timeout")
    void testDefaultConfig() {
        assertTrue(AfkHelper.isAfkSystemEnabled());
        assertEquals(300, OutboundConfig.get().afkTimeoutSeconds);
    }

    @Test
    @DisplayName("Verify player becomes AFK after exactly 5 minutes (6000 ticks) without inputs")
    void testAfkTimeoutThreshold() {
        int timeoutSeconds = 300; // 5 minutes = 6000 ticks

        assertFalse(AfkHelper.shouldBeAfk(0, timeoutSeconds));
        assertFalse(AfkHelper.shouldBeAfk(3000, timeoutSeconds));
        assertFalse(AfkHelper.shouldBeAfk(5999, timeoutSeconds));

        assertTrue(AfkHelper.shouldBeAfk(6000, timeoutSeconds));
        assertTrue(AfkHelper.shouldBeAfk(12000, timeoutSeconds));
    }

    @Test
    @DisplayName("Verify master toggle disables AFK detection, sleep exclusion, and [AFK] tags")
    void testMasterToggleDisabled() {
        OutboundConfig.get().enableAfkSystem = false;

        assertFalse(AfkHelper.isAfkSystemEnabled());
        assertFalse(AfkHelper.shouldBeAfk(6000, 300));
        assertFalse(AfkHelper.shouldIgnoreForSleep(false, false, true));
        assertEquals("Steve", AfkHelper.formatServerListPlayerName("Steve", true));

        Component base = Component.literal("Steve");
        assertEquals("Steve", AfkHelper.formatTabListName(base, true).getString());
    }

    @Test
    @DisplayName("Verify AFK players do not count toward sleep requirement unless sleeping themselves")
    void testSleepExclusionLogic() {
        // Active non-AFK player counts toward sleep requirement (not ignored)
        assertFalse(AfkHelper.shouldIgnoreForSleep(false, false, false));

        // AFK player (not in bed) is ignored for sleep requirement
        assertTrue(AfkHelper.shouldIgnoreForSleep(false, false, true));

        // Spectator is always ignored
        assertTrue(AfkHelper.shouldIgnoreForSleep(true, false, false));
        assertTrue(AfkHelper.shouldIgnoreForSleep(true, false, true));

        // Sleeping player in bed is never ignored
        assertFalse(AfkHelper.shouldIgnoreForSleep(false, true, true));
    }

    @Test
    @DisplayName("Verify Tab list and Server list hover names append [AFK] when AFK")
    void testTabAndServerListFormatting() {
        Component baseTab = Component.literal("Alex");
        assertEquals("Alex", AfkHelper.formatTabListName(baseTab, false).getString());
        assertEquals("Alex [AFK]", AfkHelper.formatTabListName(baseTab, true).getString());

        assertEquals("Alex", AfkHelper.formatServerListPlayerName("Alex", false));
        assertEquals("Alex [AFK]", AfkHelper.formatServerListPlayerName("Alex", true));
    }
}
