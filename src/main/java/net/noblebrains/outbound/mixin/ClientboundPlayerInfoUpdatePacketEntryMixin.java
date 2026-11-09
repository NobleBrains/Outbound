package net.noblebrains.outbound.mixin;

import com.mojang.authlib.GameProfile;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientboundPlayerInfoUpdatePacket.Entry.class)
public class ClientboundPlayerInfoUpdatePacketEntryMixin {

    @Redirect(
            method = "<init>(Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/level/ServerPlayer;getGameProfile()Lcom/mojang/authlib/GameProfile;"
            )
    )
    private static GameProfile outbound$useCustomNameInProfilePacket(ServerPlayer player) {
        GameProfile original = player.getGameProfile();
        String effectiveName = PlayerNametagHelper.getEffectivePlayerName(player);
        String packetName = PlayerNametagHelper.getProfilePacketName(effectiveName);
        if (!packetName.isEmpty() && !packetName.equals(original.name())) {
            return new GameProfile(original.id(), packetName, original.properties());
        }
        return original;
    }
}
