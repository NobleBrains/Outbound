package net.noblebrains.outbound.shulker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShulkerTeleportHelper {

    /**
     * Checks if the damage teleport cooldown is currently active.
     */
    public static boolean isCooldownActive(int currentTick, int lastTeleportTick, int cooldownTicks) {
        if (cooldownTicks <= 0) {
            return false;
        }
        return (currentTick - lastTeleportTick) < cooldownTicks;
    }

    /**
     * Checks whether a candidate teleport position has solid, walkable ground within maxAirBlocks below it.
     *
     * @param level the level
     * @param pos the candidate position for the shulker
     * @param maxAirBlocks the maximum allowed air blocks between candidate and ground (e.g. 4)
     * @return true if ground is found within the specified distance, false otherwise
     */
    public static boolean hasGroundWithin(Level level, BlockPos pos, int maxAirBlocks) {
        if (level == null || pos == null || maxAirBlocks < 0) {
            return true;
        }

        int airCount = 0;
        for (int dy = 1; dy <= maxAirBlocks + 1; dy++) {
            BlockPos checkPos = pos.below(dy);
            if (checkPos.getY() < level.getMinY()) {
                return false;
            }

            BlockState state = level.getBlockState(checkPos);
            if (isWalkableGround(level, checkPos, state)) {
                return airCount <= maxAirBlocks;
            }

            if (state.isAir() || state.getCollisionShape(level, checkPos).isEmpty()) {
                airCount++;
            }

            if (airCount > maxAirBlocks) {
                return false;
            }
        }

        return false;
    }

    /**
     * Determines whether a block state can act as solid, walkable ground.
     */
    public static boolean isWalkableGround(Level level, BlockPos pos, BlockState state) {
        if (state == null || state.isAir()) {
            return false;
        }
        return state.isFaceSturdy(level, pos, Direction.UP) || !state.getCollisionShape(level, pos).isEmpty();
    }

    /**
     * Checks whether there is an unobstructed line of sight (no solid block colliders)
     * between the current position and the candidate position.
     * Prevents teleporting through walls, ceilings, and floors into other rooms.
     */
    public static boolean hasLineOfSight(Level level, BlockPos fromPos, BlockPos toPos, Entity entity) {
        if (level == null || fromPos == null || toPos == null) {
            return true;
        }
        if (fromPos.equals(toPos)) {
            return true;
        }

        Vec3 from = Vec3.atCenterOf(fromPos);
        Vec3 to = Vec3.atCenterOf(toPos);
        BlockHitResult hit = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        if (hit.getType() == HitResult.Type.MISS) {
            return true;
        }

        BlockPos hitPos = hit.getBlockPos();
        return hitPos.equals(fromPos) || hitPos.equals(toPos);
    }

    /**
     * Pure validation logic for unit testing decoupled from Minecraft runtime Level.
     *
     * @param maxAirBlocks maximum air blocks allowed before hitting ground
     * @param isGroundArray array representing blocks below from top to bottom
     * @return true if ground is reached within maxAirBlocks, false otherwise
     */
    public static boolean simulateGroundCheck(int maxAirBlocks, boolean[] isGroundArray) {
        if (maxAirBlocks < 0) {
            return true;
        }
        if (isGroundArray == null || isGroundArray.length == 0) {
            return false;
        }

        int airCount = 0;
        for (boolean isGround : isGroundArray) {
            if (isGround) {
                return airCount <= maxAirBlocks;
            }
            airCount++;
            if (airCount > maxAirBlocks) {
                return false;
            }
        }
        return false;
    }

    /**
     * Pure validation logic for unit testing line-of-sight checks.
     *
     * @param hasWallBetween whether a solid wall/obstacle exists between the positions
     * @return true if line of sight is unobstructed, false otherwise
     */
    public static boolean simulateLineOfSightCheck(boolean hasWallBetween) {
        return !hasWallBetween;
    }
}
