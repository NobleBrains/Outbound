package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.end.EnderEyeHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnderEyeItem.class)
public class EnderEyeItemMixin {

    @Redirect(
            method = "use",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerLevel;findNearestMapStructure(Lnet/minecraft/tags/TagKey;Lnet/minecraft/core/BlockPos;IZ)Lnet/minecraft/core/BlockPos;"
            )
    )
    private BlockPos outbound$redirectStrongholdSearch(ServerLevel level, TagKey<Structure> tag, BlockPos playerPos, int radius, boolean skipExistingChunks) {
        if (!OutboundConfig.get().allowEnderEyeTracking) {
            return EnderEyeHelper.getRandomEyeTarget(level, playerPos);
        }
        return level.findNearestMapStructure(tag, playerPos, radius, skipExistingChunks);
    }
}
