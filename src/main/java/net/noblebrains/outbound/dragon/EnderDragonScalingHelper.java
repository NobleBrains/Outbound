package net.noblebrains.outbound.dragon;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Level;
import net.noblebrains.outbound.config.OutboundConfig;

public final class EnderDragonScalingHelper {

    public static final double FIXED_VERTICAL_ACCELERATION = 0.1D;

    private EnderDragonScalingHelper() {
    }

    /**
     * Backports the 26.4 Snapshot 8 fix (MC-272431 / MC-197201 / MC-271336 / MC-271337)
     * restoring the Ender Dragon's vertical flight acceleration multiplier from 0.01 to 0.1.
     */
    public static double getVerticalFlightAcceleration(boolean fixEnabled, double vanillaValue) {
        return fixEnabled ? FIXED_VERTICAL_ACCELERATION : vanillaValue;
    }

    /**
     * Option A (Diminishing Returns):
     * multiplier = 1.0 / (1.0 + resistancePerExtraPlayer * (activePlayers - 1))
     * - 1 player: 1.00 (0% resistance)
     * - 2 players: 0.80 (20% resistance at k=0.25)
     * - 3 players: 0.667 (33.3% resistance at k=0.25)
     * - 4 players: 0.571 (42.9% resistance at k=0.25)
     * - 5 players: 0.50 (50% resistance at k=0.25)
     */
    public static float calculateDamageMultiplier(
            boolean featureEnabled,
            int activePlayersInRadius,
            double resistancePerExtraPlayer
    ) {
        if (!featureEnabled || activePlayersInRadius <= 1 || resistancePerExtraPlayer <= 0.0) {
            return 1.0F;
        }
        double denominator = 1.0 + resistancePerExtraPlayer * (activePlayersInRadius - 1);
        return (float) (1.0 / denominator);
    }

    public static float scaleIncomingDamage(
            float baseDamage,
            boolean featureEnabled,
            int activePlayersInRadius,
            double resistancePerExtraPlayer
    ) {
        if (baseDamage <= 0.0F) {
            return baseDamage;
        }
        return baseDamage * calculateDamageMultiplier(featureEnabled, activePlayersInRadius, resistancePerExtraPlayer);
    }

    public static float scaleDragonDamageInLevel(ServerLevel level, DamageSource source, float baseDamage) {
        OutboundConfig config = OutboundConfig.get();
        if (!config.enableDragonMultiplayerScaling || level == null || baseDamage <= 0.0F) {
            return baseDamage;
        }
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return baseDamage;
        }
        int activePlayers = countActivePlayersInEndRadius(level, config.dragonKillAdvancementRadius);
        return scaleIncomingDamage(
                baseDamage,
                true,
                activePlayers,
                config.dragonResistancePerExtraPlayer
        );
    }

    /**
     * Counts alive, non-spectator players in the End dimension within maxRadius of (0, 0).
     */
    public static int countActivePlayersInEndRadius(ServerLevel level, double maxRadius) {
        if (level == null || level.getServer() == null) {
            return 0;
        }
        double maxRadiusSq = maxRadius * maxRadius;
        int count = 0;
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            if (player.isSpectator() || !player.isAlive()) {
                continue;
            }
            if (player.level().dimension() != Level.END) {
                continue;
            }
            double distSq = (player.getX() * player.getX()) + (player.getZ() * player.getZ());
            if (distSq <= maxRadiusSq) {
                count++;
            }
        }
        return count;
    }
}
