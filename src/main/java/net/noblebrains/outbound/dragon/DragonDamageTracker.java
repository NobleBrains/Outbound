package net.noblebrains.outbound.dragon;

import java.util.Set;
import java.util.UUID;

public interface DragonDamageTracker {
    Set<UUID> outbound$getDamagingPlayers();
    void outbound$recordDamagingPlayer(UUID playerUuid);
}
