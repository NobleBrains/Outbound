package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.leash.LeashImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.LeashFenceKnotEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LeashFenceKnotEntity.class)
public class LeashFenceKnotEntityMixin {
    @Inject(at = @At("HEAD"), method = "interact", cancellable = true)
    private void outbound$onInteract(Player player, InteractionHand hand, Vec3 hitPos, CallbackInfoReturnable<InteractionResult> cir) {
        if (player instanceof LeashImpl impl && impl.outbound$shouldCancel()) {
            cir.setReturnValue(InteractionResult.FAIL);
        }
    }
}
