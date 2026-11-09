package net.noblebrains.outbound.minecart;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.scores.Team;
import net.noblebrains.outbound.config.OutboundConfig;

import java.util.function.Predicate;

public class MinecartPickupHelper {

    /**
     * Wraps the entity pushable predicate.
     * When the source entity is a rideable minecart, entities in climbables (ladders, vines, scaffolding)
     * are allowed to be pushed / picked up by the minecart, decoupling minecart pickup from entity cramming immunity.
     */
    public static Predicate<Entity> wrapPushablePredicate(Entity source, Predicate<Entity> originalPredicate) {
        if (!OutboundConfig.get().enableClimbingMinecartPickup) {
            return originalPredicate;
        }

        if (!(source instanceof AbstractMinecart minecart && minecart.isRideable())) {
            return originalPredicate;
        }

        Team team = source.getTeam();
        Team.CollisionRule collisionRule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();
        if (collisionRule == Team.CollisionRule.NEVER) {
            return originalPredicate;
        }

        return target -> {
            if (originalPredicate.test(target)) {
                return true;
            }
            return canMinecartPickUpClimbingTarget(source, target);
        };
    }

    /**
     * Determines whether a living entity that is currently on a climbable should be pushable/picked up
     * by a rideable minecart.
     */
    public static boolean canMinecartPickUpClimbingTarget(Entity source, Entity target) {
        if (!(target instanceof LivingEntity living)) {
            return false;
        }

        boolean isTicking = living.isAlwaysTicking()
                || !(living.level() instanceof ServerLevel serverLevel)
                || serverLevel.isPositionEntityTicking(living.blockPosition());

        boolean isClientRemotePlayer = source.level().isClientSide()
                && (target instanceof Player player && !player.isLocalPlayer());

        Team team = source.getTeam();
        Team.CollisionRule collisionRule = team == null ? Team.CollisionRule.ALWAYS : team.getCollisionRule();

        Team team2 = target.getTeam();
        Team.CollisionRule collisionRule2 = team2 == null ? Team.CollisionRule.ALWAYS : team2.getCollisionRule();

        boolean allied = team != null && team.isAlliedTo(team2);

        return canPickUpClimbing(
                true,
                living.isAlive(),
                living.isSpectator(),
                living.isPassenger(),
                living.onClimbable(),
                isTicking,
                isClientRemotePlayer,
                collisionRule,
                collisionRule2,
                allied
        );
    }

    /**
     * Pure validation logic decoupled from entity references, allowing comprehensive unit testing.
     */
    public static boolean canPickUpClimbing(
            boolean isLiving,
            boolean isAlive,
            boolean isSpectator,
            boolean isPassenger,
            boolean onClimbable,
            boolean isPositionTicking,
            boolean isClientRemotePlayer,
            Team.CollisionRule sourceRule,
            Team.CollisionRule targetRule,
            boolean isAllied
    ) {
        if (!isLiving || !isAlive || isSpectator || isPassenger || !onClimbable) {
            return false;
        }

        if (!isPositionTicking || isClientRemotePlayer) {
            return false;
        }

        if (sourceRule == Team.CollisionRule.NEVER || targetRule == Team.CollisionRule.NEVER) {
            return false;
        }

        if ((sourceRule == Team.CollisionRule.PUSH_OWN_TEAM || targetRule == Team.CollisionRule.PUSH_OWN_TEAM) && isAllied) {
            return false;
        }

        return (sourceRule != Team.CollisionRule.PUSH_OTHER_TEAMS && targetRule != Team.CollisionRule.PUSH_OTHER_TEAMS) || isAllied;
    }
}
