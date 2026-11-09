package net.noblebrains.outbound.end;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class EnderEyeHelper {

    /**
     * Determines whether Eyes of Ender are permitted to track Strongholds.
     */
    public static boolean isEnderEyeTrackingAllowed() {
        return OutboundConfig.get().allowEnderEyeTracking;
    }

    /**
     * Generates a random target position for an Eye of Ender when tracking is disabled.
     * In the Nether or End, returns null to preserve vanilla behavior (eyes cannot be thrown there).
     * In the Overworld, picks a random direction vector so the Eye flies in a random direction.
     *
     * @param level the server level
     * @param origin the position where the eye was thrown
     * @return a random destination BlockPos in the Overworld, or null if outside the Overworld
     */
    public static BlockPos getRandomEyeTarget(ServerLevel level, BlockPos origin) {
        if (level != null && level.dimension() != Level.OVERWORLD) {
            return null;
        }
        double angle = (level != null ? level.getRandom().nextDouble() : Math.random()) * Math.PI * 2.0;
        return calculateRandomTarget(origin, angle, 500.0);
    }

    /**
     * Calculates a destination position at a given angle and horizontal distance from origin.
     * Decoupled math helper for unit testing.
     */
    public static BlockPos calculateRandomTarget(BlockPos origin, double angleRadians, double distance) {
        if (origin == null) {
            origin = BlockPos.ZERO;
        }
        int targetX = origin.getX() + (int) Math.round(Math.cos(angleRadians) * distance);
        int targetZ = origin.getZ() + (int) Math.round(Math.sin(angleRadians) * distance);
        return new BlockPos(targetX, origin.getY(), targetZ);
    }
}
