package net.noblebrains.outbound.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.noblebrains.outbound.nametag.ChunkMapTrackedRefresh;
import net.noblebrains.outbound.nametag.TrackedEntityRefresh;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ChunkMap.class)
public class ChunkMapMixin implements ChunkMapTrackedRefresh {

    @Shadow
    @Final
    ServerLevel level;

    @Shadow
    @Final
    private Int2ObjectMap<?> entityMap;

    @Override
    public void outbound$refreshPlayerEntity(ServerPlayer target) {
        Object tracked = this.entityMap.get(target.getId());
        if (tracked instanceof TrackedEntityRefresh refresh) {
            refresh.outbound$refreshPairing(List.copyOf(this.level.players()));
        }
    }
}
