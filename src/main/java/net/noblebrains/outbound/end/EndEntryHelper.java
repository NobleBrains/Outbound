package net.noblebrains.outbound.end;

import net.noblebrains.outbound.config.OutboundConfig;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class EndEntryHelper {

    /**
     * Determines whether entry into The End dimension is allowed.
     */
    public static boolean isEndEntryAllowed() {
        return OutboundConfig.get().allowEndEntry;
    }

    /**
     * Determines whether a given dimension key is The End dimension.
     */
    public static boolean isEndDimension(ResourceKey<Level> dimension) {
        if (dimension == null) {
            return false;
        }
        return dimension.identifier().getPath().equals("the_end");
    }

    /**
     * Checks whether an entity or player is allowed to transition to targetDimension.
     * - Leaving the End is always permitted.
     * - Overworld/Nether inter-dimension travel is always permitted.
     * - Entering the End from outside is only permitted if allowEndEntry is true.
     */
    public static boolean canTransitionDimension(ResourceKey<Level> currentDimension, ResourceKey<Level> targetDimension) {
        if (!isEndDimension(targetDimension)) {
            return true;
        }
        if (isEndDimension(currentDimension)) {
            // Already in the End, leaving or intra-End travel is always allowed
            return true;
        }
        return isEndEntryAllowed();
    }

    /**
     * Checks whether a player has gamemaster/operator privileges.
     */
    public static boolean isOperator(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        return Commands.LEVEL_GAMEMASTERS.check(player.permissions());
    }

    /**
     * Notifies a player when their attempt to enter the End is blocked.
     *
     * @param entity the entity attempting entry
     * @param actionbar whether to display message in actionbar overlay (true) or chat (false)
     */
    public static void notifyBlocked(Entity entity, boolean actionbar) {
        if (!OutboundConfig.get().showEndClosedMessage) {
            return;
        }
        if (entity instanceof ServerPlayer player) {
            if (!actionbar || player.tickCount % 40 == 0) {
                player.sendSystemMessage(Component.literal("\u00a7cDas End ist geschlossen!"), actionbar);
            }
        }
    }
}
