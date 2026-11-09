package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.leash.LeashImpl;
import net.noblebrains.outbound.leash.LeashProxyEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {
    @Inject(at = @At("HEAD"), method = "canUsePortal", cancellable = true)
    private void outbound$canUsePortal(boolean allowVehicles, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LeashProxyEntity) {
            cir.setReturnValue(false);
        } else if ((Object) this instanceof LeashImpl impl && impl.outbound$getHolder() != null) {
            cir.setReturnValue(false);
        }
    }
}
