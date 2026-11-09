package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.nametag.NametagRenamedPlayer;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NameTagItem.class)
public class NameTagItemMixin {

    @Inject(method = "interactLivingEntity", at = @At("HEAD"), cancellable = true)
    private void outbound$renamePlayerWithNameTag(
            ItemStack stack,
            Player player,
            LivingEntity interactionTarget,
            InteractionHand usedHand,
            CallbackInfoReturnable<InteractionResult> cir
    ) {
        if (!(interactionTarget instanceof ServerPlayer targetPlayer)) {
            return;
        }
        if (!PlayerNametagHelper.canRenamePlayer(
                PlayerNametagHelper.isFeatureEnabled(),
                targetPlayer.isAlive(),
                targetPlayer.isSpectator(),
                player.isSpectator(),
                player == targetPlayer
        )) {
            return;
        }
        Component customNameComponent = stack.get(DataComponents.CUSTOM_NAME);
        if (customNameComponent == null) {
            return;
        }
        String nametagText = customNameComponent.getString();
        String originalName = targetPlayer.getGameProfile().name();
        String currentCustom = targetPlayer instanceof NametagRenamedPlayer renamed
                ? renamed.outbound$getCustomPlayerName()
                : null;
        if (!PlayerNametagHelper.wouldChangePlayerName(originalName, currentCustom, nametagText)) {
            return;
        }
        if (!player.level().isClientSide() && targetPlayer instanceof NametagRenamedPlayer renamed) {
            String resolvedNew = PlayerNametagHelper.resolveTargetCustomName(originalName, nametagText);
            renamed.outbound$setCustomPlayerName(resolvedNew);
            stack.shrink(1);
            PlayerNametagHelper.refreshPlayerIdentity(targetPlayer);
        }
        cir.setReturnValue(InteractionResult.SUCCESS);
    }
}
