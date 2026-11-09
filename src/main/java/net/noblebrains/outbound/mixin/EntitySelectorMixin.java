package net.noblebrains.outbound.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.noblebrains.outbound.minecart.MinecartPickupHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(EntitySelector.class)
public abstract class EntitySelectorMixin {

    @Inject(method = "pushableBy", at = @At("RETURN"), cancellable = true)
    private static void outbound$allowMinecartClimbingPickup(Entity source, CallbackInfoReturnable<Predicate<Entity>> cir) {
        cir.setReturnValue(MinecartPickupHelper.wrapPushablePredicate(source, cir.getReturnValue()));
    }
}
