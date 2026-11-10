package net.noblebrains.outbound;

import net.noblebrains.outbound.farmland.FarmlandTrampleHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FarmlandTrampleTest {

    @Test
    @DisplayName("Verify protection chance scales with Feather Falling level")
    void testProtectionChance() {
        assertEquals(0.0F, FarmlandTrampleHelper.getProtectionChance(0), 0.001F);
        assertEquals(0.0F, FarmlandTrampleHelper.getProtectionChance(-1), 0.001F);
        assertEquals(0.25F, FarmlandTrampleHelper.getProtectionChance(1), 0.001F);
        assertEquals(0.50F, FarmlandTrampleHelper.getProtectionChance(2), 0.001F);
        assertEquals(0.75F, FarmlandTrampleHelper.getProtectionChance(3), 0.001F);
        assertEquals(1.00F, FarmlandTrampleHelper.getProtectionChance(4), 0.001F);
        assertEquals(1.00F, FarmlandTrampleHelper.getProtectionChance(5), 0.001F);
    }

    @Test
    @DisplayName("Verify trample protection decision based on random roll")
    void testShouldProtectFromTrample() {
        // Level 0: never protected
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(0, 0.0F));
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(0, 0.5F));

        // Level 1: 25% protection
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(1, 0.10F));
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(1, 0.24F));
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(1, 0.25F));
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(1, 0.80F));

        // Level 2: 50% protection
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(2, 0.49F));
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(2, 0.50F));

        // Level 3: 75% protection
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(3, 0.74F));
        assertFalse(FarmlandTrampleHelper.shouldProtectFromTrample(3, 0.75F));

        // Level 4: 100% protection (immune)
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(4, 0.00F));
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(4, 0.99F));
        assertTrue(FarmlandTrampleHelper.shouldProtectFromTrample(5, 0.99F));
    }
}
