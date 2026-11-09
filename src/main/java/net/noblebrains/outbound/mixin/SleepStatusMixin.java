package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.afk.AfkHelper;
import net.noblebrains.outbound.afk.AfkPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SleepStatus.class)
public class SleepStatusMixin {

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
            )
    )
    private boolean outbound$ignoreAfkPlayersForSleep(ServerPlayer player) {
        if (player.isSpectator()) {
            return true;
        }
        if (player instanceof AfkPlayer afkPlayer) {
            return AfkHelper.shouldIgnoreForSleep(false, player.isSleeping(), afkPlayer.outbound$isAfk());
        }
        return false;
    }
}
