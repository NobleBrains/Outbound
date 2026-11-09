package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.leash.LeashImpl;
import net.noblebrains.outbound.nametag.NametagRenamedPlayer;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.noblebrains.outbound.snowball.SnowballKnockbackHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin extends Avatar {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "getName", at = @At("HEAD"), cancellable = true)
    private void outbound$overridePlayerName(CallbackInfoReturnable<Component> cir) {
        if ((Object) this instanceof NametagRenamedPlayer renamed && PlayerNametagHelper.isFeatureEnabled()) {
            String customName = renamed.outbound$getCustomPlayerName();
            if (customName != null && !customName.isBlank()) {
                cir.setReturnValue(Component.literal(customName));
            }
        }
    }

    @Inject(method = "interactOn", at = @At("RETURN"), cancellable = true)
    private void outbound$onInteractPlayerLeash(
            Entity entity,
            InteractionHand hand,
            Vec3 hitPos,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (cir.getReturnValue() != InteractionResult.PASS) {
            return;
        }
        if ((Object) this instanceof ServerPlayer player && entity instanceof LeashImpl impl) {
            cir.setReturnValue(impl.outbound$interact(player, hand));
        }
    }

    @Inject(
            method = "hurtServer",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V",
                    shift = At.Shift.AFTER
            ),
            cancellable = true
    )
    private void outbound$allowSnowballPlayerKnockback(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (amount == 0.0F && SnowballKnockbackHelper.shouldKnockbackPlayerWithZeroDamage(level, source)) {
            cir.setReturnValue(super.hurtServer(level, source, 0.0F));
        }
    }
}
