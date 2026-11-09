package net.noblebrains.outbound.leash;

import net.minecraft.world.phys.Vec3;

public class PlayerLeashHelper {

    public static final int DETACH_GRACE_TICKS = 20;

    /**
     * Pure validation logic for whether a player can be leashed.
     */
    public static boolean canPlayerBeLeashed(
            boolean featureEnabled,
            boolean isAlive,
            boolean isSpectator
    ) {
        return featureEnabled && isAlive && !isSpectator;
    }

    /**
     * Checks whether a right-click interaction by the current leash holder is allowed to detach the leash.
     * Enforces a 20-tick grace period so the client's off-hand interaction packet does not immediately
     * detach a leash that was just attached with the main hand.
     */
    public static boolean canDetachAfterGracePeriod(int lastAttachTick, int currentTick) {
        return lastAttachTick + DETACH_GRACE_TICKS < currentTick;
    }

    /**
     * Calculates the proxy entity position at the target player's neck so the lead rope attaches naturally.
     */
    public static Vec3 calculateNeckPosition(Vec3 playerPos, float xRot, boolean sneaking) {
        double y = xRot > 31.0F ? 1.9D : 1.3D;
        if (sneaking) {
            y -= 0.5D;
        }
        return playerPos.add(0.0D, y, -0.15D);
    }

    /**
     * Calculates the pull velocity vector applied to a leashed player towards their holder.
     */
    public static Vec3 calculatePullVector(Vec3 playerPos, Vec3 holderPos, double distance) {
        if (distance <= 0.0D) {
            return Vec3.ZERO;
        }
        double dx = (holderPos.x - playerPos.x) / distance;
        double dy = (holderPos.y - playerPos.y) / distance;
        double dz = (holderPos.z - playerPos.z) / distance;

        return new Vec3(
                Math.copySign(dx * dx * 0.4D, dx),
                Math.copySign(dy * dy * 0.4D, dy),
                Math.copySign(dz * dz * 0.4D, dz)
        );
    }
}
