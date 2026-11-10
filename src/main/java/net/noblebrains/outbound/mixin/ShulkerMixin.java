package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.shulker.ShulkerTeleportHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Shulker.class)
public abstract class ShulkerMixin {

    @Shadow
    protected abstract Direction findAttachableSurface(BlockPos pos);

    @Unique
    private boolean outbound$isPlayerDamageContext = false;

    @Unique
    private int outbound$lastPlayerDamageTeleportTick = -100000;

    @Inject(method = "hurtServer", at = @At("HEAD"))
    private void outbound$beforeHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (source != null && source.getEntity() instanceof Player) {
            this.outbound$isPlayerDamageContext = true;
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void outbound$afterHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        this.outbound$isPlayerDamageContext = false;
    }

    /**
     * Prevents player-damage-induced teleportation while the cooldown is active.
     * Detached (floating) shulkers are exempt so they can immediately reattach.
     */
    @Inject(method = "teleportSomewhere", at = @At("HEAD"), cancellable = true)
    private void outbound$checkPlayerDamageTeleportCooldown(CallbackInfoReturnable<Boolean> cir) {
        if (!this.outbound$isPlayerDamageContext) {
            return;
        }

        OutboundConfig config = OutboundConfig.get();
        if (!config.enableShulkerDamageTeleportAdjustments) {
            return;
        }

        Shulker self = (Shulker) (Object) this;
        boolean isCurrentlyAttached = this.findAttachableSurface(self.blockPosition()) != null;
        if (!isCurrentlyAttached) {
            return;
        }

        if (ShulkerTeleportHelper.isCooldownActive(self.tickCount, this.outbound$lastPlayerDamageTeleportTick, config.shulkerTeleportCooldownTicks)) {
            cir.setReturnValue(false);
        }
    }

    /**
     * Increases the search attempts in teleportSomewhere during player damage teleportation so that finding
     * an accessible position in the same room with ground nearby succeeds reliably.
     */
    @ModifyConstant(
            method = "teleportSomewhere",
            constant = @Constant(intValue = 5)
    )
    private int outbound$modifyMaxTeleportAttempts(int original) {
        return (this.outbound$isPlayerDamageContext && OutboundConfig.get().enableShulkerDamageTeleportAdjustments) ? 16 : original;
    }

    /**
     * Filters candidate teleport positions during player damage to ensure they are in the same room (unobstructed line of sight)
     * and easily accessible from the ground (maximum configured air blocks to solid ground below).
     */
    @Redirect(
            method = "teleportSomewhere",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/monster/Shulker;findAttachableSurface(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/core/Direction;"
            )
    )
    private Direction outbound$filterGroundAccessibleSurface(Shulker shulker, BlockPos candidate) {
        Direction dir = this.findAttachableSurface(candidate);
        if (dir == null) {
            return null;
        }

        if (this.outbound$isPlayerDamageContext && OutboundConfig.get().enableShulkerDamageTeleportAdjustments) {
            boolean isCurrentlyAttached = this.findAttachableSurface(shulker.blockPosition()) != null;
            if (isCurrentlyAttached) {
                OutboundConfig config = OutboundConfig.get();
                if (config.shulkerTeleportRequiresLineOfSight
                        && !ShulkerTeleportHelper.hasLineOfSight(shulker.level(), shulker.blockPosition(), candidate, shulker)) {
                    return null;
                }

                int maxGroundDist = config.shulkerMaxGroundDistance;
                if (maxGroundDist >= 0 && !ShulkerTeleportHelper.hasGroundWithin(shulker.level(), candidate, maxGroundDist)) {
                    return null;
                }
            }
        }

        return dir;
    }

    /**
     * Records the tick when a player-damage-induced teleport successfully occurs.
     */
    @Inject(method = "teleportSomewhere", at = @At("RETURN"))
    private void outbound$recordPlayerDamageTeleport(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && this.outbound$isPlayerDamageContext) {
            Shulker self = (Shulker) (Object) this;
            this.outbound$lastPlayerDamageTeleportTick = self.tickCount;
        }
    }
}
