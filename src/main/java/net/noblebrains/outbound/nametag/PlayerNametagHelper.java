package net.noblebrains.outbound.nametag;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.nio.charset.StandardCharsets;
import java.util.List;

public final class PlayerNametagHelper {
    public static final int MAX_PROFILE_PACKET_NAME_LENGTH = 16;

    private PlayerNametagHelper() {
    }

    public static boolean isFeatureEnabled() {
        return OutboundConfig.get().enablePlayerNametagRenaming;
    }

    public static boolean canRenamePlayer(
            boolean featureEnabled,
            boolean targetAlive,
            boolean targetSpectator,
            boolean actorSpectator,
            boolean isSelf
    ) {
        return featureEnabled && targetAlive && !targetSpectator && !actorSpectator && !isSelf;
    }

    public static String normalizeCustomName(String rawName) {
        if (rawName == null) {
            return null;
        }
        String trimmed = rawName.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static boolean isOriginalPlayerName(String originalProfileName, String candidateName) {
        String normalizedCandidate = normalizeCustomName(candidateName);
        if (originalProfileName == null || normalizedCandidate == null) {
            return false;
        }
        return originalProfileName.equalsIgnoreCase(normalizedCandidate);
    }

    /**
     * Resolves the new custom name to store when a nametag with {@code nametagText} is used on a player.
     * Returns {@code null} if the nametag matches the player's original name (resetting to original).
     */
    public static String resolveTargetCustomName(String originalProfileName, String nametagText) {
        String normalized = normalizeCustomName(nametagText);
        if (normalized == null) {
            return null;
        }
        if (isOriginalPlayerName(originalProfileName, normalized)) {
            return null;
        }
        return normalized;
    }

    /**
     * Checks whether applying a nametag with {@code nametagText} actually changes the player's state.
     */
    public static boolean wouldChangePlayerName(String originalProfileName, String currentCustomName, String nametagText) {
        String normalizedNametag = normalizeCustomName(nametagText);
        if (normalizedNametag == null) {
            return false;
        }
        String normalizedCurrent = normalizeCustomName(currentCustomName);
        String resolvedNew = resolveTargetCustomName(originalProfileName, normalizedNametag);
        if (resolvedNew == null) {
            return normalizedCurrent != null;
        }
        return !resolvedNew.equals(normalizedCurrent);
    }

    public static String getEffectivePlayerName(String originalProfileName, String customName, boolean featureEnabled) {
        if (featureEnabled) {
            String normalized = normalizeCustomName(customName);
            if (normalized != null) {
                return normalized;
            }
        }
        return originalProfileName;
    }

    public static String getEffectivePlayerName(ServerPlayer player) {
        String original = player.getGameProfile().name();
        if (player instanceof NametagRenamedPlayer renamed) {
            return getEffectivePlayerName(original, renamed.outbound$getCustomPlayerName(), isFeatureEnabled());
        }
        return original;
    }

    /**
     * Truncates a name so it is guaranteed to satisfy {@code ByteBufCodecs.PLAYER_NAME} (max 16 chars / 48 UTF-8 bytes).
     */
    public static String getProfilePacketName(String effectiveName) {
        if (effectiveName == null || effectiveName.isEmpty()) {
            return "";
        }
        String result = effectiveName.length() <= MAX_PROFILE_PACKET_NAME_LENGTH
                ? effectiveName
                : effectiveName.substring(0, MAX_PROFILE_PACKET_NAME_LENGTH);
        while (result.getBytes(StandardCharsets.UTF_8).length > MAX_PROFILE_PACKET_NAME_LENGTH * 3 && !result.isEmpty()) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    public static void refreshPlayerIdentity(ServerPlayer target) {
        MinecraftServer server = target.level().getServer();
        if (server == null) {
            return;
        }
        PlayerList playerList = server.getPlayerList();
        if (playerList != null) {
            playerList.broadcastAll(new ClientboundPlayerInfoRemovePacket(List.of(target.getUUID())));
            playerList.broadcastAll(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(target)));
        }
        ChunkMap chunkMap = target.level().getChunkSource().chunkMap;
        if (chunkMap instanceof ChunkMapTrackedRefresh refresh) {
            refresh.outbound$refreshPlayerEntity(target);
        }
        server.invalidateStatus();
    }

    public static void refreshAllOnlinePlayers(MinecraftServer server) {
        if (server == null || server.getPlayerList() == null) {
            return;
        }
        for (ServerPlayer player : List.copyOf(server.getPlayerList().getPlayers())) {
            refreshPlayerIdentity(player);
        }
    }
}
