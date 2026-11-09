package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/world/entity/monster/Enderman$EndermanLeaveBlockGoal"})
public class EndermanLeaveBlockMixin {
    @Inject(method = "canUse", at = @At("HEAD"), cancellable = true)
    private void outbound$preventLeaveBlock(CallbackInfoReturnable<Boolean> cir) {
        if (OutboundConfig.get().preventEndermanGrief) {
            cir.setReturnValue(false);
        }
    }
}
