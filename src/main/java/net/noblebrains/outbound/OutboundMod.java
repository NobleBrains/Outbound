package net.noblebrains.outbound;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.noblebrains.outbound.command.OutboundCommand;
import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.light.LightMarkerHelper;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutboundMod implements ModInitializer {
    public static final String MOD_ID = "outbound";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Outbound: Vanilla-faithful server-side enhancements...");

        // Load or initialize config
        OutboundConfig.load();

        // Register world tick handler for light block particle markers
        ServerTickEvents.END_LEVEL_TICK.register(level -> {
            if (OutboundConfig.get().enableLightBlockSurvivalBreak) {
                for (ServerPlayer player : level.players()) {
                    LightMarkerHelper.tickPlayer(player);
                }
            }
        });

        // Register server commands
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            OutboundCommand.register(dispatcher);
        });

        LOGGER.info("Outbound fully initialized with high-efficiency ticking architecture!");
    }
}
