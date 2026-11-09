package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.afk.AfkHelper;
import net.noblebrains.outbound.afk.AfkPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.List;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @ModifyArg(
            method = "updateSleepingPlayerList",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/players/SleepStatus;update(Ljava/util/List;)Z"
            ),
            index = 0
    )
    private List<ServerPlayer> outbound$filterAfkPlayersForSleepStatus(List<ServerPlayer> players) {
        if (!AfkHelper.isAfkSystemEnabled() || players == null || players.isEmpty()) {
            return players;
        }
        return players.stream()
                .filter(player -> {
                    boolean isAfk = player instanceof AfkPlayer afkPlayer && afkPlayer.outbound$isAfk();
                    return !AfkHelper.shouldIgnoreForSleep(player.isSpectator(), player.isSleeping(), isAfk);
                })
                .toList();
    }
}
