package net.noblebrains.outbound.xp;

import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.noblebrains.outbound.config.OutboundConfig;

public final class XpBottlingHandler {

    private XpBottlingHandler() {
    }

    public static void register() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!OutboundConfig.get().enableXpBottling) {
                return InteractionResult.PASS;
            }
            if (!(player instanceof ServerPlayer serverPlayer)) {
                return InteractionResult.PASS;
            }
            if (tryBottleXp(serverPlayer, hand)) {
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }

    public static boolean tryBottleXp(ServerPlayer player, InteractionHand hand) {
        if (!player.isShiftKeyDown()) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(Items.GLASS_BOTTLE)) {
            return false;
        }

        int cost = Math.max(1, OutboundConfig.get().xpBottlingCost);
        if (!player.hasInfiniteMaterials()) {
            int currentXp = getPlayerXp(player);
            if (currentXp < cost) {
                return false;
            }
            setPlayerXp(player, currentXp - cost);
        }

        ItemStack xpBottle = new ItemStack(Items.EXPERIENCE_BOTTLE);
        if (held.getCount() == 1) {
            player.setItemInHand(hand, xpBottle);
        } else {
            held.shrink(1);
            if (!player.getInventory().add(xpBottle)) {
                player.drop(xpBottle, false, Prediction.SERVER_ONLY);
            }
        }

        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.BOTTLE_FILL,
                SoundSource.PLAYERS,
                0.8f,
                1.0f
        );
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.PLAYERS,
                0.35f,
                0.85f
        );
        return true;
    }

    public static int getPlayerXp(Player player) {
        int base = getExperienceForLevel(player.experienceLevel);
        int progress = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return Math.max(0, base + progress);
    }

    public static void setPlayerXp(ServerPlayer player, int totalXp) {
        int clamped = Math.max(0, totalXp);
        int newLevel = getLevelForExperience(clamped);
        int expForLevel = getExperienceForLevel(newLevel);

        player.setExperienceLevels(newLevel);
        player.totalExperience = clamped;
        int needed = player.getXpNeededForNextLevel();
        player.experienceProgress = needed > 0 ? Math.min(1.0f, Math.max(0.0f, (clamped - expForLevel) / (float) needed)) : 0.0f;
    }

    public static int getExperienceForLevel(int level) {
        if (level <= 0) {
            return 0;
        }
        if (level < 16) {
            return level * level + 6 * level;
        }
        if (level < 32) {
            return (int) (2.5 * level * level - 40.5 * level + 360);
        }
        return (int) (4.5 * level * level - 162.5 * level + 2220);
    }

    public static int getLevelForExperience(int experience) {
        if (experience <= 0) {
            return 0;
        }
        int level = 0;
        while (getExperienceForLevel(level + 1) <= experience) {
            level++;
        }
        return level;
    }
}
