package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.farmland.FarmlandTrampleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FarmlandBlock.class)
public abstract class FarmlandBlockMixin {

    @Inject(method = "turnToBaseBlock", at = @At("HEAD"), cancellable = true)
    private void outbound$preventTrampleWithFeatherFalling(Entity entity, BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        if (entity != null && OutboundConfig.get().featherFallingProtectsFarmland) {
            int levelEnchant = FarmlandTrampleHelper.getHighestFeatherFallingLevel(entity);
            if (levelEnchant > 0 && FarmlandTrampleHelper.shouldProtectFromTrample(levelEnchant, level.getRandom().nextFloat())) {
                ci.cancel();
            }
        }
    }
}
