package net.noblebrains.outbound.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.dragon.DragonDamageTracker;
import net.noblebrains.outbound.dragon.EnderDragonScalingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin implements DragonDamageTracker {

    @Unique
    private final Set<UUID> outbound$damagingPlayers = new HashSet<>();

    /**
     * Backports the 26.4 Snapshot 8 Ender Dragon flight fix (MC-272431 / MC-197201 / MC-271336 / MC-271337),
     * restoring vertical flight acceleration from 0.01D to 0.1D.
     */
    @ModifyConstant(method = "aiStep", constant = @Constant(doubleValue = 0.01D))
    private double outbound$fixVerticalFlightAcceleration(double original) {
        return EnderDragonScalingHelper.getVerticalFlightAcceleration(
                OutboundConfig.get().fixEnderDragonFlight,
                original
        );
    }

    /**
     * Scales incoming damage to the Ender Dragon based on the number of active players
     * in the End within the fight radius (Option A: Diminishing Returns).
     */
    @ModifyVariable(method = "reallyHurt", at = @At("HEAD"), argsOnly = true)
    private float outbound$scaleDamageWithPlayerCount(float damage, ServerLevel level, DamageSource source) {
        return EnderDragonScalingHelper.scaleDragonDamageInLevel(level, source, damage);
    }

    @Inject(method = "reallyHurt", at = @At("HEAD"))
    private void outbound$trackDamagingPlayer(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        if (source != null) {
            if (source.getEntity() instanceof ServerPlayer player) {
                this.outbound$recordDamagingPlayer(player.getUUID());
            } else if (source.getDirectEntity() instanceof ServerPlayer directPlayer) {
                this.outbound$recordDamagingPlayer(directPlayer.getUUID());
            }
        }
    }

    @Override
    public Set<UUID> outbound$getDamagingPlayers() {
        return Collections.unmodifiableSet(this.outbound$damagingPlayers);
    }

    @Override
    public void outbound$recordDamagingPlayer(UUID playerUuid) {
        if (playerUuid != null) {
            this.outbound$damagingPlayers.add(playerUuid);
        }
    }
}
