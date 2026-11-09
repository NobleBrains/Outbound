package net.noblebrains.outbound.light;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class LightMarkerHelper {
    private static final int HORIZONTAL_RADIUS = 10;
    private static final int VERTICAL_RADIUS = 6;

    public static void tickPlayer(ServerPlayer player) {
        if (player.tickCount % 30 != 0) {
            return;
        }
        if (player.isCreative() || player.isSpectator() || !player.isHolding(Items.LIGHT)) {
            return;
        }
        ServerLevel level = player.level();
        BlockPos playerPos = player.blockPosition();
        BlockPos min = playerPos.offset(-HORIZONTAL_RADIUS, -VERTICAL_RADIUS, -HORIZONTAL_RADIUS);
        BlockPos max = playerPos.offset(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!state.is(Blocks.LIGHT)) continue;
            level.sendParticles(player, new BlockParticleOption(ParticleTypes.BLOCK_MARKER, state), true, true, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
