package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = {"net/minecraft/world/level/block/state/BlockBehaviour$BlockStateBase"})
public abstract class BlockBehaviourBlockStateBaseMixin {
    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    private void outbound$allowLightBreakInSurvival(Player player, BlockGetter level, BlockPos pos, CallbackInfoReturnable<Float> cir) {
        BlockState state = (BlockState) (Object) this;
        if (state.is(Blocks.LIGHT) && OutboundConfig.get().enableLightBlockSurvivalBreak && player != null && player.isHolding(Items.LIGHT)) {
            cir.setReturnValue(1.0f);
        }
    }
}
