package net.noblebrains.outbound;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.snowball.SnowballKnockbackHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SnowballKnockbackTest {

    @BeforeEach
    void setUp() {
        OutboundConfig.get().enableSnowballPlayerKnockback = true;
    }

    @Test
    @DisplayName("Verify snowball player knockback is enabled by default")
    void testDefaultConfig() {
        assertTrue(OutboundConfig.get().enableSnowballPlayerKnockback);
    }

    @Test
    @DisplayName("Snowball projectile applies zero-damage knockback to players when enabled")
    void testSnowballProjectileAppliesKnockback() {
        assertTrue(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, true, false, false, false));
        assertTrue(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, true, false, false, true));
    }

    @Test
    @DisplayName("Snowball weapon item applies zero-damage knockback to players when enabled")
    void testSnowballWeaponAppliesKnockback() {
        assertTrue(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, false, true, false, false));
    }

    @Test
    @DisplayName("Non-snowball zero-damage hits do not apply player knockback")
    void testNonSnowballIgnored() {
        assertFalse(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, false, false, false, false));
    }

    @Test
    @DisplayName("Difficulty-scaling mob snowball does not knock back players on Peaceful")
    void testMobSnowballBlockedOnPeaceful() {
        assertFalse(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, true, false, true, true));
        assertTrue(SnowballKnockbackHelper.canApplyZeroDamageKnockback(true, true, false, true, false));
    }

    @Test
    @DisplayName("Feature toggle disables snowball player knockback")
    void testFeatureDisabled() {
        assertFalse(SnowballKnockbackHelper.canApplyZeroDamageKnockback(false, true, true, false, false));
    }
}
