package net.noblebrains.outbound.snowball;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class SnowballKnockbackHelper {

    /**
     * Determines whether a zero-damage hit on a player should still proceed to apply knockback
     * (backporting the Minecraft 26.4 snowball player knockback behavior).
     */
    public static boolean shouldKnockbackPlayerWithZeroDamage(ServerLevel level, DamageSource source) {
        if (!OutboundConfig.get().enableSnowballPlayerKnockback || source == null) {
            return false;
        }

        boolean scalesWithDifficulty = source.scalesWithDifficulty();
        boolean isPeaceful = level != null && level.getDifficulty() == Difficulty.PEACEFUL;
        boolean isSnowballProjectile = source.getDirectEntity() instanceof Snowball;

        ItemStack weaponItem = source.getWeaponItem();
        boolean isSnowballWeapon = weaponItem != null && weaponItem.is(Items.SNOWBALL);

        return canApplyZeroDamageKnockback(
                OutboundConfig.get().enableSnowballPlayerKnockback,
                isSnowballProjectile,
                isSnowballWeapon,
                scalesWithDifficulty,
                isPeaceful
        );
    }

    /**
     * Pure validation logic decoupled from Minecraft runtime instances for unit testing.
     */
    public static boolean canApplyZeroDamageKnockback(
            boolean featureEnabled,
            boolean isSnowballProjectile,
            boolean isSnowballWeapon,
            boolean scalesWithDifficulty,
            boolean isPeaceful
    ) {
        if (!featureEnabled) {
            return false;
        }
        if (scalesWithDifficulty && isPeaceful) {
            return false;
        }
        return isSnowballProjectile || isSnowballWeapon;
    }
}
