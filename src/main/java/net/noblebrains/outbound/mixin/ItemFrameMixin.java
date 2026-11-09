package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.itemframe.ItemFrameInvisibilityHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrame.class)
public abstract class ItemFrameMixin {

    @Shadow
    private boolean fixed;

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract SoundEvent getRotateItemSound();

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void outbound$shiftRightClickInvisible(
            Player player,
            InteractionHand hand,
            Vec3 location,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        ItemFrame self = (ItemFrame) (Object) this;
        if (ItemFrameInvisibilityHelper.canTurnInvisibleOnShiftInteract(
                ItemFrameInvisibilityHelper.isFeatureEnabled(),
                this.fixed,
                !this.getItem().isEmpty(),
                self.isInvisible(),
                player.isSecondaryUseActive()
        )) {
            if (!player.level().isClientSide()) {
                self.setInvisible(true);
                self.playSound(this.getRotateItemSound(), 1.0F, 1.0F);
                self.gameEvent(GameEvent.BLOCK_CHANGE, player);
            }
            cir.setReturnValue(InteractionResult.SUCCESS);
        }
    }

    @Inject(method = "setItem(Lnet/minecraft/world/item/ItemStack;Z)V", at = @At("TAIL"))
    private void outbound$makeVisibleWhenItemRemoved(ItemStack value, boolean updateNeighbours, CallbackInfo ci) {
        ItemFrame self = (ItemFrame) (Object) this;
        if (ItemFrameInvisibilityHelper.shouldResetInvisibilityOnItemChange(!value.isEmpty(), self.isInvisible())) {
            self.setInvisible(false);
        }
    }
}
