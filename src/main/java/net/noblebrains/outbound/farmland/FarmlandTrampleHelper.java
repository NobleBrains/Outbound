package net.noblebrains.outbound.farmland;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class FarmlandTrampleHelper {

    private FarmlandTrampleHelper() {
    }

    /**
     * Calculates the protection chance based on Feather Falling level:
     * - Level 0: 0% protection
     * - Level 1: 25% protection
     * - Level 2: 50% protection
     * - Level 3: 75% protection
     * - Level 4+: 100% protection
     */
    public static float getProtectionChance(int featherFallingLevel) {
        if (featherFallingLevel <= 0) {
            return 0.0F;
        }
        if (featherFallingLevel >= 4) {
            return 1.0F;
        }
        return featherFallingLevel * 0.25F;
    }

    /**
     * Determines whether trampling should be prevented given a random roll in [0.0, 1.0).
     */
    public static boolean shouldProtectFromTrample(int featherFallingLevel, float randomRoll) {
        if (featherFallingLevel <= 0) {
            return false;
        }
        if (featherFallingLevel >= 4) {
            return true;
        }
        return randomRoll < getProtectionChance(featherFallingLevel);
    }

    /**
     * Retrieves the highest Feather Falling level between an entity and all its passengers.
     */
    public static int getHighestFeatherFallingLevel(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return 0;
        }
        int max = getFeatherFallingLevel(living);
        for (Entity passenger : living.getPassengers()) {
            if (passenger instanceof LivingEntity livingPassenger) {
                max = Math.max(max, getFeatherFallingLevel(livingPassenger));
            }
        }
        return max;
    }

    /**
     * Reads the Feather Falling enchantment level from an entity's equipped boots.
     */
    public static int getFeatherFallingLevel(LivingEntity living) {
        if (living == null) {
            return 0;
        }
        ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
        if (boots != null && !boots.isEmpty()) {
            ItemEnchantments enchants = boots.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            for (Holder<Enchantment> holder : enchants.keySet()) {
                if (holder.is(Enchantments.FEATHER_FALLING)) {
                    return enchants.getLevel(holder);
                }
            }
        }
        return 0;
    }
}
