package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.portal.CustomPortalValidator;
import net.minecraft.world.level.portal.PortalShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PortalShape.class)
public abstract class PortalShapeMixin {

    @Shadow
    private int width;

    @Shadow
    private int height;

    @ModifyConstant(
            method = "calculateWidth",
            constant = @Constant(intValue = 2)
    )
    private static int outbound$modifyMinWidth(int original) {
        return OutboundConfig.get().enableCustomPortals ? 1 : original;
    }

    @ModifyConstant(
            method = "calculateHeight",
            constant = @Constant(intValue = 3)
    )
    private static int outbound$modifyMinHeight(int original) {
        return OutboundConfig.get().enableCustomPortals ? 1 : original;
    }

    @Inject(
            method = "isValid",
            at = @At("HEAD"),
            cancellable = true
    )
    private void outbound$isValid(CallbackInfoReturnable<Boolean> cir) {
        if (OutboundConfig.get().enableCustomPortals) {
            cir.setReturnValue(CustomPortalValidator.isAllowedSize(this.width, this.height));
        }
    }
}
