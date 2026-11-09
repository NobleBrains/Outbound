package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.afk.AfkHelper;
import net.noblebrains.outbound.afk.AfkPlayer;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {

    @Redirect(
            method = "buildPlayerStatus",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;nameAndId()Lnet/minecraft/server/players/NameAndId;"
            )
    )
    private NameAndId outbound$appendAfkToServerListSample(ServerPlayer player) {
        NameAndId original = player.nameAndId();
        String effectiveName = PlayerNametagHelper.getEffectivePlayerName(player);
        boolean isAfk = player instanceof AfkPlayer afkPlayer && afkPlayer.outbound$isAfk();
        String formatted = AfkHelper.formatServerListPlayerName(effectiveName, isAfk);
        if (!formatted.equals(original.name())) {
            return new NameAndId(original.id(), formatted);
        }
        return original;
    }
}
