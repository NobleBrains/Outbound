package net.noblebrains.outbound.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.noblebrains.outbound.dragon.DragonDamageTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Mixin(EnderDragon.class)
public abstract class EnderDragonMixin implements DragonDamageTracker {

    @Unique
    private final Set<UUID> outbound$damagingPlayers = new HashSet<>();

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
