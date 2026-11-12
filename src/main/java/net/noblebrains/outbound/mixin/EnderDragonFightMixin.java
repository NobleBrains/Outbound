package net.noblebrains.outbound.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.dimension.end.EnderDragonFight;
import net.noblebrains.outbound.dragon.SharedDragonAdvancementHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderDragonFight.class)
public abstract class EnderDragonFightMixin {

    @Inject(method = "setDragonKilled", at = @At("TAIL"))
    private void outbound$onDragonKilled(EnderDragon dragon, CallbackInfo ci) {
        if (dragon != null && dragon.level() instanceof ServerLevel serverLevel) {
            SharedDragonAdvancementHelper.rewardEligiblePlayers(serverLevel, dragon);
        }
    }
}
