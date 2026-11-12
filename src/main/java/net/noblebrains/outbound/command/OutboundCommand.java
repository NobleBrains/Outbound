package net.noblebrains.outbound.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.noblebrains.outbound.config.OutboundConfig;
import net.noblebrains.outbound.motd.MotdHelper;
import net.noblebrains.outbound.nametag.PlayerNametagHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class OutboundCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> outboundRoot = Commands.literal("outbound")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(ctx -> executeStatusOverview(ctx.getSource()))
                .then(Commands.literal("reload")
                        .executes(ctx -> {
                            OutboundConfig.load();
                            PlayerNametagHelper.refreshAllOnlinePlayers(ctx.getSource().getServer());
                            ctx.getSource().sendSuccess(() -> Component.literal("\u00a7a[Outbound] Configuration reloaded successfully!"), true);
                            return 1;
                        })
                )
                .then(createMotdCommand())
                .then(createEndCommand())
                .then(createEndMessageCommand())
                .then(createEyeTrackingCommand())
                .then(createAfkCommand())
                .then(createPlayerNametagCommand())
                .then(createInvisibleItemFramesCommand())
                .then(createPlayerLeashCommand())
                .then(createSnowballKnockbackCommand())
                .then(createFeatherFallingFarmlandCommand())
                .then(createLightBreakCommand())
                .then(createEndermanGriefCommand())
                .then(createSharedDragonAdvancementCommand())
                .then(createDragonFlightFixCommand())
                .then(createDragonScalingCommand());

        dispatcher.register(outboundRoot);
    }

    private static int executeStatusOverview(CommandSourceStack source) {
        var cfg = OutboundConfig.get();
        source.sendSuccess(() -> Component.literal(
                "\u00a76=== Outbound Commands & Status ===\n" +
                "\u00a7e/outbound reload \u00a77- Reload configuration\n" +
                "\u00a7e/outbound motd [text] \u00a77- Live Server MOTD\n" +
                "\u00a7e/outbound allowEnd [bool|toggle] \u00a77- End access: " + (cfg.allowEndEntry ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound endMessage [bool|toggle] \u00a77- End closed popup: " + (cfg.showEndClosedMessage ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound allowEyeTracking [bool|toggle] \u00a77- Ender Eye tracking: " + (cfg.allowEnderEyeTracking ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound afk [bool|toggle] \u00a77- AFK system: " + (cfg.enableAfkSystem ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound playerNametag [bool|toggle] \u00a77- Name Tag renaming: " + (cfg.enablePlayerNametagRenaming ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound invisibleItemFrames [bool|toggle] \u00a77- Invisible item frames: " + (cfg.enableInvisibleItemFrames ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound playerLeashing [bool|toggle] \u00a77- Player leashing: " + (cfg.enablePlayerLeashing ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound snowballKnockback [bool|toggle] \u00a77- Snowball knockback: " + (cfg.enableSnowballPlayerKnockback ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound featherFallingFarmland [bool|toggle] \u00a77- Farmland protection: " + (cfg.featherFallingProtectsFarmland ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound lightBreak [bool|toggle] \u00a77- Light block survival break: " + (cfg.enableLightBlockSurvivalBreak ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound endermanGrief [bool|toggle] \u00a77- Enderman grief prevention: " + (cfg.preventEndermanGrief ? "\u00a7aPREVENTED" : "\u00a7cALLOWED") + "\n" +
                "\u00a7e/outbound sharedDragonAdvancement [bool|toggle] \u00a77- Shared Dragon kill advancement: " + (cfg.enableSharedDragonKillAdvancement ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound dragonFlightFix [bool|toggle] \u00a77- Dragon flight fix (26.4 backport): " + (cfg.fixEnderDragonFlight ? "\u00a7aENABLED" : "\u00a7cDISABLED") + "\n" +
                "\u00a7e/outbound dragonScaling [bool|toggle] \u00a77- Dragon multiplayer scaling: " + (cfg.enableDragonMultiplayerScaling ? "\u00a7aENABLED" : "\u00a7cDISABLED")
        ), false);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createMotdCommand() {
        return Commands.literal("motd")
                .executes(ctx -> executeQueryMotd(ctx.getSource()))
                .then(Commands.argument("motd", StringArgumentType.greedyString())
                        .executes(ctx -> executeSetMotd(ctx.getSource(), StringArgumentType.getString(ctx, "motd")))
                );
    }

    private static int executeQueryMotd(CommandSourceStack source) {
        String current = MotdHelper.getServerMotd(source.getServer());
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Aktuelle Server-MOTD:\n\u00a7r" + current), false);
        return 1;
    }

    private static int executeSetMotd(CommandSourceStack source, String rawMotd) {
        MotdHelper.setServerMotd(source.getServer(), rawMotd);
        String formatted = MotdHelper.formatMotd(rawMotd);
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Server-MOTD erfolgreich aktualisiert:\n\u00a7r" + formatted), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createEndCommand() {
        return Commands.literal("allowEnd")
                .executes(ctx -> executeQueryEnd(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetEnd(ctx.getSource(), !OutboundConfig.get().allowEndEntry)))
                .then(Commands.argument("allowed", BoolArgumentType.bool())
                        .executes(ctx -> executeSetEnd(ctx.getSource(), BoolArgumentType.getBool(ctx, "allowed")))
                );
    }

    private static int executeQueryEnd(CommandSourceStack source) {
        boolean allowed = OutboundConfig.get().allowEndEntry;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] End access: " + (allowed ? "\u00a7aENABLED (players can enter the End)" : "\u00a7cDISABLED (End entry blocked)")), false);
        return 1;
    }

    private static int executeSetEnd(CommandSourceStack source, boolean allowed) {
        OutboundConfig.get().allowEndEntry = allowed;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set End access to: " + allowed + (allowed ? " (\u00a7aEnd entry ENABLED\u00a7r)" : " (\u00a7cEnd entry DISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createEndMessageCommand() {
        return Commands.literal("endMessage")
                .executes(ctx -> executeQueryEndMessage(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetEndMessage(ctx.getSource(), !OutboundConfig.get().showEndClosedMessage)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetEndMessage(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryEndMessage(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().showEndClosedMessage;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] End closed message popup: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetEndMessage(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().showEndClosedMessage = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set End closed message popup to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createEyeTrackingCommand() {
        return Commands.literal("allowEyeTracking")
                .executes(ctx -> executeQueryEyeTracking(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetEyeTracking(ctx.getSource(), !OutboundConfig.get().allowEnderEyeTracking)))
                .then(Commands.argument("allowed", BoolArgumentType.bool())
                        .executes(ctx -> executeSetEyeTracking(ctx.getSource(), BoolArgumentType.getBool(ctx, "allowed")))
                );
    }

    private static int executeQueryEyeTracking(CommandSourceStack source) {
        boolean allowed = OutboundConfig.get().allowEnderEyeTracking;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Ender Eye Stronghold tracking: " + (allowed ? "\u00a7aENABLED (tracks Strongholds)" : "\u00a7cDISABLED (flies in random directions)")), false);
        return 1;
    }

    private static int executeSetEyeTracking(CommandSourceStack source, boolean allowed) {
        OutboundConfig.get().allowEnderEyeTracking = allowed;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Ender Eye tracking to: " + allowed + (allowed ? " (\u00a7aENABLED - tracks Strongholds\u00a7r)" : " (\u00a7cDISABLED - flies in random directions\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createAfkCommand() {
        return Commands.literal("afk")
                .executes(ctx -> executeQueryAfk(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetAfk(ctx.getSource(), !OutboundConfig.get().enableAfkSystem)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetAfk(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryAfk(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableAfkSystem;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] AFK system: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetAfk(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableAfkSystem = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set AFK system to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createPlayerNametagCommand() {
        return Commands.literal("playerNametag")
                .executes(ctx -> executeQueryPlayerNametag(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetPlayerNametag(ctx.getSource(), !OutboundConfig.get().enablePlayerNametagRenaming)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetPlayerNametag(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryPlayerNametag(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enablePlayerNametagRenaming;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Player Name Tag renaming: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetPlayerNametag(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enablePlayerNametagRenaming = enabled;
        OutboundConfig.save();
        PlayerNametagHelper.refreshAllOnlinePlayers(source.getServer());
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Player Name Tag renaming to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createInvisibleItemFramesCommand() {
        return Commands.literal("invisibleItemFrames")
                .executes(ctx -> executeQueryInvisibleItemFrames(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetInvisibleItemFrames(ctx.getSource(), !OutboundConfig.get().enableInvisibleItemFrames)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetInvisibleItemFrames(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryInvisibleItemFrames(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableInvisibleItemFrames;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Shift+Right-Click invisible Item Frames: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetInvisibleItemFrames(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableInvisibleItemFrames = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Shift+Right-Click invisible Item Frames to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createPlayerLeashCommand() {
        return Commands.literal("playerLeashing")
                .executes(ctx -> executeQueryPlayerLeashing(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetPlayerLeashing(ctx.getSource(), !OutboundConfig.get().enablePlayerLeashing)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetPlayerLeashing(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryPlayerLeashing(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enablePlayerLeashing;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Player leashing: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetPlayerLeashing(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enablePlayerLeashing = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Player leashing to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSnowballKnockbackCommand() {
        return Commands.literal("snowballKnockback")
                .executes(ctx -> executeQuerySnowballKnockback(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetSnowballKnockback(ctx.getSource(), !OutboundConfig.get().enableSnowballPlayerKnockback)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetSnowballKnockback(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQuerySnowballKnockback(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableSnowballPlayerKnockback;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Snowball player knockback: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetSnowballKnockback(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableSnowballPlayerKnockback = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Snowball player knockback to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createFeatherFallingFarmlandCommand() {
        return Commands.literal("featherFallingFarmland")
                .executes(ctx -> executeQueryFeatherFallingFarmland(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetFeatherFallingFarmland(ctx.getSource(), !OutboundConfig.get().featherFallingProtectsFarmland)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetFeatherFallingFarmland(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryFeatherFallingFarmland(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().featherFallingProtectsFarmland;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Feather Falling farmland protection: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetFeatherFallingFarmland(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().featherFallingProtectsFarmland = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Feather Falling farmland protection to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createLightBreakCommand() {
        return Commands.literal("lightBreak")
                .executes(ctx -> executeQueryLightBreak(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetLightBreak(ctx.getSource(), !OutboundConfig.get().enableLightBlockSurvivalBreak)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetLightBreak(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryLightBreak(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableLightBlockSurvivalBreak;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Light block survival breaking: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetLightBreak(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableLightBlockSurvivalBreak = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Light block survival breaking to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createEndermanGriefCommand() {
        return Commands.literal("endermanGrief")
                .executes(ctx -> executeQueryEndermanGrief(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetEndermanGrief(ctx.getSource(), !OutboundConfig.get().preventEndermanGrief)))
                .then(Commands.argument("prevented", BoolArgumentType.bool())
                        .executes(ctx -> executeSetEndermanGrief(ctx.getSource(), BoolArgumentType.getBool(ctx, "prevented")))
                );
    }

    private static int executeQueryEndermanGrief(CommandSourceStack source) {
        boolean prevented = OutboundConfig.get().preventEndermanGrief;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Enderman grief prevention: " + (prevented ? "\u00a7aENABLED (blocks safe)" : "\u00a7cDISABLED (vanilla griefing)")), false);
        return 1;
    }

    private static int executeSetEndermanGrief(CommandSourceStack source, boolean prevented) {
        OutboundConfig.get().preventEndermanGrief = prevented;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Enderman grief prevention to: " + prevented + (prevented ? " (\u00a7aENABLED - blocks safe\u00a7r)" : " (\u00a7cDISABLED - vanilla griefing\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSharedDragonAdvancementCommand() {
        return Commands.literal("sharedDragonAdvancement")
                .executes(ctx -> executeQuerySharedDragonAdvancement(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetSharedDragonAdvancement(ctx.getSource(), !OutboundConfig.get().enableSharedDragonKillAdvancement)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetSharedDragonAdvancement(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQuerySharedDragonAdvancement(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableSharedDragonKillAdvancement;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Shared Ender Dragon kill advancement: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetSharedDragonAdvancement(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableSharedDragonKillAdvancement = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Shared Ender Dragon kill advancement to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createDragonFlightFixCommand() {
        return Commands.literal("dragonFlightFix")
                .executes(ctx -> executeQueryDragonFlightFix(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetDragonFlightFix(ctx.getSource(), !OutboundConfig.get().fixEnderDragonFlight)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetDragonFlightFix(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryDragonFlightFix(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().fixEnderDragonFlight;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Ender Dragon 26.4 flight fix: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetDragonFlightFix(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().fixEnderDragonFlight = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Ender Dragon 26.4 flight fix to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createDragonScalingCommand() {
        return Commands.literal("dragonScaling")
                .executes(ctx -> executeQueryDragonScaling(ctx.getSource()))
                .then(Commands.literal("toggle")
                        .executes(ctx -> executeSetDragonScaling(ctx.getSource(), !OutboundConfig.get().enableDragonMultiplayerScaling)))
                .then(Commands.argument("enabled", BoolArgumentType.bool())
                        .executes(ctx -> executeSetDragonScaling(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                );
    }

    private static int executeQueryDragonScaling(CommandSourceStack source) {
        boolean enabled = OutboundConfig.get().enableDragonMultiplayerScaling;
        source.sendSuccess(() -> Component.literal("\u00a76[Outbound] Ender Dragon multiplayer resistance scaling: " + (enabled ? "\u00a7aENABLED" : "\u00a7cDISABLED")), false);
        return 1;
    }

    private static int executeSetDragonScaling(CommandSourceStack source, boolean enabled) {
        OutboundConfig.get().enableDragonMultiplayerScaling = enabled;
        OutboundConfig.save();
        source.sendSuccess(() -> Component.literal("\u00a7a[Outbound] Set Ender Dragon multiplayer resistance scaling to: " + enabled + (enabled ? " (\u00a7aENABLED\u00a7r)" : " (\u00a7cDISABLED\u00a7r)")), true);
        return 1;
    }
}
