package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.nametag.TrackedEntityRefresh;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(targets = "net.minecraft.server.level.ChunkMap$TrackedEntity")
public abstract class ChunkMapTrackedEntityMixin implements TrackedEntityRefresh {

    @Shadow
    public abstract void removePlayer(ServerPlayer player);

    @Shadow
    public abstract void updatePlayers(List<ServerPlayer> players);

    @Override
    public void outbound$refreshPairing(List<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            this.removePlayer(player);
        }
        this.updatePlayers(players);
    }
}
