package net.noblebrains.outbound.mixin;

import net.noblebrains.outbound.afk.AfkHelper;
import net.noblebrains.outbound.afk.AfkPlayer;
import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.end.EndEntryHelper;
import net.noblebrains.outbound.leash.LeashImpl;
import net.noblebrains.outbound.leash.LeashProxyEntity;
import net.noblebrains.outbound.leash.PlayerLeashHelper;
import net.noblebrains.outbound.nametag.NametagRenamedPlayer;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin implements LeashImpl, AfkPlayer, NametagRenamedPlayer {

    @Shadow
    public abstract boolean hasDisconnected();

    @Unique
    private LeashProxyEntity outbound$proxy;

    @Unique
    private Entity outbound$holder;

    @Unique
    private int outbound$lastage;

    @Unique
    private int outbound$lastActiveInputTick = 0;

    @Unique
    private boolean outbound$isAfk = false;

    @Unique
    private float outbound$lastYRot = Float.NaN;

    @Unique
    private float outbound$lastXRot = Float.NaN;

    @Unique
    private String outbound$customPlayerName = null;

    @Override
    public String outbound$getCustomPlayerName() {
        return this.outbound$customPlayerName;
    }

    @Override
    public void outbound$setCustomPlayerName(String customName) {
        this.outbound$customPlayerName = PlayerNametagHelper.normalizeCustomName(customName);
    }

    @Unique
    private ServerPlayer outbound$getSelf() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public Entity outbound$getHolder() {
        return this.outbound$holder;
    }

    @Override
    public boolean outbound$shouldCancel() {
        return outbound$getHolder() != null && !OutboundConfig.get().allowLeashedRemoveFenceKnot;
    }

    @Unique
    private boolean outbound$disabled() {
        return !OutboundConfig.get().enablePlayerLeashing;
    }

    @Unique
    private void outbound$update() {
        ServerPlayer self = outbound$getSelf();
        if (
                outbound$holder != null && (
                        outbound$disabled()
                        || !outbound$holder.isAlive()
                        || hasDisconnected()
                        || self.isPassenger()
                        || self.isSpectator()
                )
        ) {
            outbound$detach();
            outbound$drop();
        }

        if (outbound$proxy != null) {
            if (outbound$proxy.proxyIsRemoved()) {
                outbound$proxy = null;
            } else {
                outbound$proxy.setInvisible(true);
                Entity holderActual = outbound$holder;
                Entity holderTarget = outbound$proxy.getLeashHolder();

                if (holderTarget == null && holderActual != null) {
                    outbound$detach();
                    outbound$drop();
                } else if (holderTarget != holderActual) {
                    outbound$attach(holderTarget);
                }
            }
        }

        outbound$apply();
    }

    @Unique
    private void outbound$apply() {
        ServerPlayer player = outbound$getSelf();
        Entity holder = outbound$holder;
        if (holder == null) return;
        if (holder.level() != player.level()) return;

        float distance = player.distanceTo(holder);
        if (distance < OutboundConfig.get().playerLeashDistanceMin) {
            return;
        }
        if (distance > OutboundConfig.get().playerLeashDistanceMax) {
            outbound$detach();
            outbound$drop();
            return;
        }

        Vec3 pull = PlayerLeashHelper.calculatePullVector(player.position(), holder.position(), distance);
        player.push(pull.x, pull.y, pull.z);

        player.connection.send(new ClientboundSetEntityMotionPacket(player));
        player.syncVelocity = false;
        player.connection.resetFlyingTicks();
    }

    @Unique
    private void outbound$attach(Entity holder) {
        outbound$holder = holder;
        ServerPlayer leashed = outbound$getSelf();

        if (outbound$proxy == null) {
            outbound$proxy = new LeashProxyEntity(leashed);
            leashed.level().addFreshEntity(outbound$proxy);
            outbound$proxy.snapTo(leashed.getX(), leashed.getY(), leashed.getZ(), 0.0F, 0.0F);
            outbound$proxy.snapTo(new Vec3(leashed.getX(), leashed.getY(), leashed.getZ()));
        }
        outbound$proxy.setLeashedTo(outbound$holder, true);
        outbound$lastage = leashed.tickCount;
    }

    @Unique
    private void outbound$detach() {
        outbound$holder = null;

        if (outbound$proxy != null) {
            if (outbound$proxy.isAlive() || !outbound$proxy.proxyIsRemoved()) {
                outbound$proxy.proxyRemove();
            }
            outbound$proxy = null;
        }
    }

    @Unique
    private void outbound$drop() {
        ServerPlayer self = outbound$getSelf();
        self.spawnAtLocation(self.level(), Items.LEAD);
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void outbound$tick(CallbackInfo info) {
        outbound$update();
        outbound$updateAfk();
    }

    @Override
    public boolean outbound$isAfk() {
        return this.outbound$isAfk && AfkHelper.isAfkSystemEnabled();
    }

    @Override
    public void outbound$recordActiveInput() {
        ServerPlayer self = outbound$getSelf();
        this.outbound$lastActiveInputTick = self.tickCount;
        if (this.outbound$isAfk) {
            this.outbound$setAfkState(false);
        }
    }

    @Inject(method = "resetLastActionTime", at = @At("HEAD"))
    private void outbound$onResetLastActionTime(CallbackInfo ci) {
        this.outbound$recordActiveInput();
    }

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    private void outbound$modifyTabListDisplayName(CallbackInfoReturnable<Component> cir) {
        ServerPlayer self = outbound$getSelf();
        boolean hasCustomName = PlayerNametagHelper.isFeatureEnabled()
                && this.outbound$customPlayerName != null
                && !this.outbound$customPlayerName.isBlank();
        boolean isAfk = this.outbound$isAfk();
        if (hasCustomName || isAfk) {
            Component base = cir.getReturnValue() != null ? cir.getReturnValue() : self.getDisplayName();
            cir.setReturnValue(AfkHelper.formatTabListName(base, isAfk));
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void outbound$saveCustomPlayerName(ValueOutput output, CallbackInfo ci) {
        if (this.outbound$customPlayerName != null && !this.outbound$customPlayerName.isBlank()) {
            output.putString("OutboundCustomPlayerName", this.outbound$customPlayerName);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void outbound$loadCustomPlayerName(ValueInput input, CallbackInfo ci) {
        String loadedName = PlayerNametagHelper.normalizeCustomName(input.getStringOr("OutboundCustomPlayerName", ""));
        String originalName = outbound$getSelf().getGameProfile().name();
        this.outbound$customPlayerName = PlayerNametagHelper.resolveTargetCustomName(originalName, loadedName);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void outbound$restoreCustomPlayerName(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof NametagRenamedPlayer oldRenamed) {
            this.outbound$customPlayerName = oldRenamed.outbound$getCustomPlayerName();
        }
    }

    @Unique
    private void outbound$updateAfk() {
        ServerPlayer self = outbound$getSelf();
        if (!AfkHelper.isAfkSystemEnabled()) {
            if (this.outbound$isAfk) {
                this.outbound$setAfkState(false);
            }
            this.outbound$lastActiveInputTick = self.tickCount;
            return;
        }

        float currentYRot = self.getYRot();
        float currentXRot = self.getXRot();
        if (!Float.isNaN(this.outbound$lastYRot)) {
            if (Math.abs(currentYRot - this.outbound$lastYRot) > 0.05f
                    || Math.abs(currentXRot - this.outbound$lastXRot) > 0.05f) {
                this.outbound$recordActiveInput();
            }
        }
        this.outbound$lastYRot = currentYRot;
        this.outbound$lastXRot = currentXRot;

        if (!self.getLastClientInput().equals(Input.EMPTY)) {
            this.outbound$recordActiveInput();
        }

        int idleTicks = Math.max(0, self.tickCount - this.outbound$lastActiveInputTick);
        boolean shouldBeAfk = AfkHelper.shouldBeAfk(idleTicks, OutboundConfig.get().afkTimeoutSeconds);
        if (shouldBeAfk != this.outbound$isAfk) {
            this.outbound$setAfkState(shouldBeAfk);
        }
    }

    @Unique
    private void outbound$setAfkState(boolean afk) {
        if (this.outbound$isAfk == afk) {
            return;
        }
        this.outbound$isAfk = afk;
        ServerPlayer self = outbound$getSelf();
        MinecraftServer server = self.level().getServer();
        if (server != null) {
            if (server.getPlayerList() != null) {
                server.getPlayerList().broadcastAll(
                        new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, self)
                );
            }
            server.invalidateStatus();
        }
        self.level().updateSleepingPlayerList();
    }

    @Override
    public InteractionResult outbound$interact(Player player, InteractionHand hand) {
        ServerPlayer self = outbound$getSelf();
        if (!PlayerLeashHelper.canPlayerBeLeashed(OutboundConfig.get().enablePlayerLeashing, self.isAlive(), self.isSpectator())) {
            return InteractionResult.PASS;
        }
        if (player == self || player.isSpectator()) {
            return InteractionResult.PASS;
        }

        ItemStack stack = player.getItemInHand(hand);
        if (stack.is(Items.LEAD) && outbound$holder == null) {
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            outbound$attach(player);
            return InteractionResult.SUCCESS;
        }

        if (outbound$holder == player && PlayerLeashHelper.canDetachAfterGracePeriod(outbound$lastage, self.tickCount)) {
            if (!player.isCreative()) {
                outbound$drop();
            }
            outbound$detach();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Inject(method = "disconnect", at = @At("RETURN"))
    private void outbound$disconnect(CallbackInfo ci) {
        if (outbound$holder != null) {
            outbound$detach();
            outbound$drop();
        }
    }

    @Inject(method = "die", at = @At("RETURN"))
    private void outbound$onDeath(CallbackInfo ci) {
        if (outbound$holder != null) {
            outbound$detach();
            outbound$drop();
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"), cancellable = true)
    private void outbound$preventTeleportToLevelEnd(ServerLevel newLevel, double x, double y, double z, Set<Relative> relativeList, float yRot, float xRot, boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = outbound$getSelf();
        if (newLevel != null && !EndEntryHelper.canTransitionDimension(player.level().dimension(), newLevel.dimension())) {
            if (!EndEntryHelper.isOperator(player)) {
                EndEntryHelper.notifyBlocked(player, false);
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At("HEAD"), cancellable = true)
    private void outbound$preventTransitionToEnd(TeleportTransition transition, CallbackInfoReturnable<ServerPlayer> cir) {
        ServerPlayer player = outbound$getSelf();
        if (transition != null && transition.newLevel() != null && !EndEntryHelper.canTransitionDimension(player.level().dimension(), transition.newLevel().dimension())) {
            if (!EndEntryHelper.isOperator(player)) {
                EndEntryHelper.notifyBlocked(player, false);
                cir.setReturnValue(null);
            }
        }
    }
}
