package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.leash.LeashImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.BlockAttachedEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockAttachedEntity.class)
public class BlockAttachedEntityMixin {
    @Inject(at = @At("HEAD"), method = "skipAttackInteraction", cancellable = true)
    private void outbound$onInteractBlockDefaultTrigger(Entity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (attacker instanceof LeashImpl impl && impl.outbound$shouldCancel()) {
            cir.setReturnValue(true);
        }
    }
}
