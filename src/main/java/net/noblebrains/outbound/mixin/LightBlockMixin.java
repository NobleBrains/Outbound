package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LightBlock.class)
public abstract class LightBlockMixin {

    @Inject(method = "useWithoutItem", at = @At("HEAD"), cancellable = true)
    private void outbound$cycleLightInSurvival(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!level.isClientSide()
                && OutboundConfig.get().enableLightBlockSurvivalBreak
                && player != null
                && !player.canUseGameMasterBlocks()
                && player.isHolding(Items.LIGHT)) {
            BlockState newState = state.cycle(LightBlock.LEVEL);
            level.setBlock(pos, newState, 2);
            if (player instanceof ServerPlayer serverPlayer && level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        serverPlayer,
                        new BlockParticleOption(ParticleTypes.BLOCK_MARKER, newState),
                        true,
                        true,
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        1,
                        0.0, 0.0, 0.0, 0.0
                );
            }
            cir.setReturnValue(InteractionResult.SUCCESS_SERVER);
        }
    }
}
