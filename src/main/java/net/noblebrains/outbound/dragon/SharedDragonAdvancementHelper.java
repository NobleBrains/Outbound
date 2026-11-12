package net.noblebrains.outbound.dragon;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.level.Level;
import net.noblebrains.outbound.config.OutboundConfig;

import java.util.Set;
import java.util.UUID;

public class SharedDragonAdvancementHelper {

    public static final Identifier KILL_DRAGON_ADVANCEMENT = Identifier.withDefaultNamespace("end/kill_dragon");

    /**
     * Checks if a player is eligible for the shared dragon kill advancement:
     * - Feature must be enabled in config
     * - Player must have damaged the dragon during the fight
     * - Player must be in the End dimension
     * - Player must be within maxRadius blocks of (0, 0)
     */
    public static boolean isEligibleForAdvancement(
            boolean featureEnabled,
            boolean hasDamagedDragon,
            boolean isInEndDimension,
            double x,
            double z,
            double maxRadius
    ) {
        if (!featureEnabled || !hasDamagedDragon || !isInEndDimension) {
            return false;
        }
        double distSq = (x * x) + (z * z);
        return distSq <= (maxRadius * maxRadius);
    }

    public static void rewardEligiblePlayers(ServerLevel level, EnderDragon dragon) {
        OutboundConfig config = OutboundConfig.get();
        if (!config.enableSharedDragonKillAdvancement || level == null || dragon == null) {
            return;
        }

        if (!(dragon instanceof DragonDamageTracker tracker)) {
            return;
        }

        Set<UUID> damagers = tracker.outbound$getDamagingPlayers();
        if (damagers.isEmpty()) {
            return;
        }

        double maxRadius = config.dragonKillAdvancementRadius;
        DamageSource damageSource = dragon.getLastDamageSource() != null
                ? dragon.getLastDamageSource()
                : level.damageSources().generic();

        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            boolean hasDamaged = damagers.contains(player.getUUID());
            boolean isInEnd = player.level().dimension() == Level.END;

            if (isEligibleForAdvancement(true, hasDamaged, isInEnd, player.getX(), player.getZ(), maxRadius)) {
                CriteriaTriggers.PLAYER_KILLED_ENTITY.trigger(player, dragon, damageSource);
                grantAdvancementDirectly(player);
            }
        }
    }

    public static void grantAdvancementDirectly(ServerPlayer player) {
        if (player == null || player.level() == null || player.level().getServer() == null) {
            return;
        }
        AdvancementHolder holder = player.level().getServer().getAdvancements().get(KILL_DRAGON_ADVANCEMENT);
        if (holder != null) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(holder);
            if (!progress.isDone()) {
                for (String criterion : progress.getRemainingCriteria()) {
                    player.getAdvancements().award(holder, criterion);
                }
            }
        }
    }
}
