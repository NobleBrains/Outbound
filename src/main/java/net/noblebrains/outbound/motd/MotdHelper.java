package net.noblebrains.outbound.motd;

import net.minecraft.server.MinecraftServer;

import java.util.regex.Pattern;

public final class MotdHelper {

    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    private MotdHelper() {
    }

    /**
     * Formats user input for MOTD:
     * - Translates '&' color/format codes to '§'
     * - Replaces literal '\n' sequences with real newline characters
     */
    public static String formatMotd(String input) {
        if (input == null) {
            return "";
        }
        String withNewlines = input.replace("\\n", "\n");
        return COLOR_CODE_PATTERN.matcher(withNewlines).replaceAll("\u00a7$1");
    }

    /**
     * Updates the server MOTD dynamically at runtime and invalidates cached server status.
     * On DedicatedServer, this also persists the new MOTD to server.properties immediately.
     */
    public static void setServerMotd(MinecraftServer server, String rawMotd) {
        if (server == null) {
            return;
        }
        String formatted = formatMotd(rawMotd);
        server.setMotd(formatted);
        server.invalidateStatus();
    }

    /**
     * Retrieves the current server MOTD.
     */
    public static String getServerMotd(MinecraftServer server) {
        if (server == null) {
            return "";
        }
        String motd = server.getMotd();
        return motd != null ? motd : "";
    }
}
