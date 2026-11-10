package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Block.class)
public abstract class BlockMixin {
    @Inject(method = "playerDestroy", at = @At("TAIL"))
    private void outbound$dropLightOnBreak(ServerLevel level, ServerPlayer player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack tool, CallbackInfo ci) {
        if (state.is(Blocks.LIGHT) && OutboundConfig.get().enableLightBlockSurvivalBreak && !player.isCreative()) {
            ItemStack drop = LightBlock.setLightOnStack(new ItemStack(Items.LIGHT), 15);
            Block.popResource(level, pos, drop);
        }
    }
}
