package net.noblebrains.outbound.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.noblebrains.outbound.OutboundMod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OutboundConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path getConfigPath() {
        try {
            if (FabricLoader.getInstance() != null && FabricLoader.getInstance().getConfigDir() != null) {
                return FabricLoader.getInstance().getConfigDir().resolve("outbound.json");
            }
        } catch (Throwable ignored) {
        }
        return Path.of("config", "outbound.json");
    }

    private static OutboundConfig INSTANCE = new OutboundConfig();

    // Feature toggles
    public boolean enableCustomPortals = true;
    public boolean allowAnyCustomPortalSize = false;
    public boolean enableClimbingMinecartPickup = true;
    public boolean enableShulkerDamageTeleportAdjustments = true;
    public int shulkerTeleportCooldownTicks = 80;
    public int shulkerMaxGroundDistance = 4;
    public boolean shulkerTeleportRequiresLineOfSight = true;
    public boolean preventEndermanGrief = true;
    public boolean enableLightBlockSurvivalBreak = true;
    public boolean featherFallingProtectsFarmland = true;
    public boolean allowEndEntry = true;
    public boolean showEndClosedMessage = false;
    public boolean allowEnderEyeTracking = true;
    public boolean enableSnowballPlayerKnockback = true;
    public boolean enablePlayerLeashing = true;
    public double playerLeashDistanceMin = 4.0;
    public double playerLeashDistanceMax = 10.0;
    public boolean allowLeashedRemoveFenceKnot = false;
    public boolean enableAfkSystem = true;
    public int afkTimeoutSeconds = 300;
    public boolean enablePlayerNametagRenaming = true;
    public boolean enableInvisibleItemFrames = true;
    public boolean enableSharedDragonKillAdvancement = true;
    public double dragonKillAdvancementRadius = 200.0;
    public boolean fixEnderDragonFlight = true;
    public boolean enableDragonMultiplayerScaling = true;
    public double dragonResistancePerExtraPlayer = 0.25;

    // Debugging
    public boolean debugLogging = false;

    public static OutboundConfig get() {
        return INSTANCE;
    }

    public static void load() {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            try {
                Path legacy = path.getParent() != null ? path.getParent().resolve("wanderlust.json") : Path.of("config", "wanderlust.json");
                if (Files.exists(legacy)) {
                    try (BufferedReader reader = Files.newBufferedReader(legacy)) {
                        OutboundConfig loaded = GSON.fromJson(reader, OutboundConfig.class);
                        if (loaded != null) {
                            INSTANCE = loaded;
                            OutboundMod.LOGGER.info("Migrated legacy Wanderlust configuration to Outbound successfully.");
                            save();
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (Files.exists(path)) {
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                OutboundConfig loaded = GSON.fromJson(reader, OutboundConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                    OutboundMod.LOGGER.info("Outbound configuration loaded successfully.");
                    return;
                }
            } catch (Exception e) {
                OutboundMod.LOGGER.error("Failed to read outbound.json, using defaults.", e);
            }
        }
        save();
    }

    public static void save() {
        try {
            Path path = getConfigPath();
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(path)) {
                GSON.toJson(INSTANCE, writer);
            }
        } catch (IOException e) {
            OutboundMod.LOGGER.error("Failed to save outbound.json", e);
        }
    }
}
