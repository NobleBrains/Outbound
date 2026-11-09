package net.noblebrains.outbound.leash;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public interface LeashImpl {
    Entity outbound$getHolder();
    boolean outbound$shouldCancel();
    InteractionResult outbound$interact(Player player, InteractionHand hand);
}
