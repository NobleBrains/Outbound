package net.noblebrains.outbound.nametag;

import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public interface TrackedEntityRefresh {
    void outbound$refreshPairing(List<ServerPlayer> players);
}
