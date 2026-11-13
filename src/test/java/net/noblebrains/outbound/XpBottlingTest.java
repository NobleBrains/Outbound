package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.xp.XpBottlingHandler;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class XpBottlingTest {

    @Test
    void vanillaLevelXpFormulaMatchesMilestones() {
        assertEquals(0, XpBottlingHandler.getExperienceForLevel(0));
        assertEquals(7, XpBottlingHandler.getExperienceForLevel(1));
        assertEquals(315, XpBottlingHandler.getExperienceForLevel(15));
        assertEquals(1395, XpBottlingHandler.getExperienceForLevel(30));
    }

    @Test
    void levelLookupInvertsExperienceThresholds() {
        assertEquals(0, XpBottlingHandler.getLevelForExperience(0));
        assertEquals(0, XpBottlingHandler.getLevelForExperience(6));
        assertEquals(1, XpBottlingHandler.getLevelForExperience(7));
        assertEquals(15, XpBottlingHandler.getLevelForExperience(315));
        assertEquals(30, XpBottlingHandler.getLevelForExperience(1395));
        assertEquals(29, XpBottlingHandler.getLevelForExperience(1394));
    }

    @Test
    void xpBottlingDefaultsAreBalancedAndDisabled() {
        assertFalse(OutboundConfig.get().enableXpBottling);
        assertEquals(25, OutboundConfig.get().xpBottlingCost);
    }
}
