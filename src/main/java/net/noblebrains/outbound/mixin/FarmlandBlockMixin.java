/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.core.component.DataComponents
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.item.enchantment.ItemEnchantments
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.FarmlandBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  org.spongepowered.asm.mixin.Mixin
 *  org.spongepowered.asm.mixin.Unique
 *  org.spongepowered.asm.mixin.injection.At
 *  org.spongepowered.asm.mixin.injection.Inject
 *  org.spongepowered.asm.mixin.injection.callback.CallbackInfo
 */
package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FarmlandBlock.class})
public abstract class FarmlandBlockMixin {
    @Inject(method={"turnToBaseBlock"}, at={@At(value="HEAD")}, cancellable=true)
    private void outbound$preventTrampleWithFeatherFalling(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity != null && OutboundConfig.get().featherFallingProtectsFarmland && FarmlandBlockMixin.outbound$isProtectedFromTrampling(entity)) {
            ci.cancel();
        }
    }

    @Unique
    private static boolean outbound$isProtectedFromTrampling(Entity entity) {
        if (entity instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            if (FarmlandBlockMixin.outbound$hasFeatherFalling(living)) {
                return true;
            }
            for (Entity passenger : living.getPassengers()) {
                LivingEntity livingPassenger;
                if (!(passenger instanceof LivingEntity) || !FarmlandBlockMixin.outbound$hasFeatherFalling(livingPassenger = (LivingEntity)passenger)) continue;
                return true;
            }
        }
        return false;
    }

    @Unique
    private static boolean outbound$hasFeatherFalling(LivingEntity living) {
        ItemStack boots = living.getItemBySlot(EquipmentSlot.FEET);
        if (boots != null && !boots.isEmpty()) {
            ItemEnchantments enchants = (ItemEnchantments)boots.getOrDefault(DataComponents.ENCHANTMENTS, (Object)ItemEnchantments.EMPTY);
            for (Holder holder : enchants.keySet()) {
                if (!holder.is(Enchantments.FEATHER_FALLING)) continue;
                return true;
            }
        }
        return false;
    }
}

