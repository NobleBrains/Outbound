package net.noblebrains.outbound.nametag;

import net.minecraft.server.level.ServerPlayer;

public interface ChunkMapTrackedRefresh {
    void outbound$refreshPlayerEntity(ServerPlayer target);
}
