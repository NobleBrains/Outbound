package net.noblebrains.outbound.afk;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class AfkHelper {

    public static final String AFK_SUFFIX = " [AFK]";

    /**
     * Checks whether the AFK system is currently enabled in configuration.
     */
    public static boolean isAfkSystemEnabled() {
        return OutboundConfig.get().enableAfkSystem;
    }

    /**
     * Determines whether a player should be classified as AFK based on their idle ticks.
     *
     * @param idleTicks number of ticks since the player's last active input
     * @param afkTimeoutSeconds configured timeout in seconds (e.g. 300 for 5 minutes)
     * @return true if the player has exceeded the timeout while the AFK system is enabled
     */
    public static boolean shouldBeAfk(int idleTicks, int afkTimeoutSeconds) {
        if (!isAfkSystemEnabled() || afkTimeoutSeconds <= 0) {
            return false;
        }
        return idleTicks >= (afkTimeoutSeconds * 20);
    }

    /**
     * Determines whether a player should be ignored when calculating the number of players
     * required to sleep through the night.
     *
     * @param isSpectator whether the player is in spectator mode
     * @param isSleeping whether the player is currently sleeping in a bed
     * @param isAfk whether the player is currently AFK
     * @return true if the player should not be counted toward required sleepers
     */
    public static boolean shouldIgnoreForSleep(boolean isSpectator, boolean isSleeping, boolean isAfk) {
        if (isSpectator) {
            return true;
        }
        if (isSleeping) {
            return false;
        }
        return isAfk && isAfkSystemEnabled();
    }

    /**
     * Formats the in-game Tab list display name with the [AFK] suffix when the player is AFK.
     */
    public static Component formatTabListName(Component baseName, boolean isAfk) {
        if (!isAfk || !isAfkSystemEnabled() || baseName == null) {
            return baseName;
        }
        return baseName.copy().append(Component.literal(AFK_SUFFIX).withStyle(ChatFormatting.GRAY));
    }

    /**
     * Formats the player's name for the server list hover sample with the [AFK] suffix when AFK.
     */
    public static String formatServerListPlayerName(String baseName, boolean isAfk) {
        if (baseName == null) {
            return "";
        }
        if (!isAfk || !isAfkSystemEnabled()) {
            return baseName;
        }
        return baseName + AFK_SUFFIX;
    }
}
