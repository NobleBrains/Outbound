# Outbound

Outbound is a server-side utility and gameplay adjustment mod for Minecraft Fabric. It adds refined vanilla mechanics, multiplayer quality-of-life improvements, and server management tools without altering core vanilla behavior or requiring any client-side installations.

Because Outbound runs entirely on the server, players can join using standard, unmodded vanilla Minecraft clients.

---

## Why Outbound?

Many server utility mods either require custom client software or alter vanilla gameplay mechanics drastically. Outbound is designed for server owners who want:

* **Zero Client Requirements:** Players connect using a standard, unmodded vanilla Minecraft client. No mod installation or custom resource pack is needed on the user's end.
* **Vanilla-Faithful Gameplay:** Enhancements are integrated naturally into existing mechanics (such as leads, portals, minecarts) rather than introducing custom items or GUIs.
* **Administrative Control:** All the features can be individually toggled, configured, or updated at runtime without needing to reboot the server.

---

## Why This Mod Exists

I wanted to pick up villagers with a minecart while keeping them inside a holding chamber that used a vine to bypass the mob cramming limit. However, whenever a minecart tried to pick up the villagers, they simply did not enter it.

After investigating, I discovered the cause: in vanilla Minecraft, entities standing in climbable blocks are marked as unpushable to avoid cramming damage, but the minecart passenger logic also relies on that exact check. Because of this, climbable entities cannot enter minecarts under normal circumstances.

That led to the creation of Outbound: to decouple minecart boarding from cramming immunity and solve similar rough edges in vanilla gameplay while keeping the vanilla feeling completely intact.

---

## Features in Detail

### Decoupled Minecart Pickup on Climbables
In vanilla Minecraft, `LivingEntity.isPushable()` unconditionally returns `false` when an entity is on a climbable block (ladders, vines, weeping vines, twisting vines, cave vines, scaffolding, and trapdoors above ladders). This prevents mob cramming, but also prevents moving minecarts from picking up entities.
* **Decoupled Logic:** Outbound preserves mob cramming immunity, while allowing moving rideable minecarts to collect mobs from climbable surfaces.
* **Wide Support:** Works seamlessly across ladders, vines, scaffolding, and weeping/twisting vines.
* **Movement Engine Compatible:** Compatible with both traditional minecart physics and the experimental New Minecart Movement.

### Compact and Custom Nether Portals
Vanilla Nether Portals require a frame opening of at least 2 blocks wide and 3 blocks high, up to a maximum of 21x21 blocks. This mod expands portal ignition logic to allow more compact portals:
* **1x1 Portals:** Single-block openings cool for crawl-in entrances, secret pathways, compact item transit, and compact redstone contraptions.
* **1x2 Portals:** Door-sized vertical portals suitable for compact hallways, bases, and aesthetic builds.
* **2x2 Portals:** Compact square portal openings.
* **Vanilla Portals:** Standard 2x3 through 21x21 portals remain completely functional, with optional corner blocks.

### Fair Shulker Teleportation
Combat teleportation for Shulkers often causes them to clip through solid walls, into inaccessible closed rooms, or float out over the void. Outbound refines shulker combat teleportation while maintaining vanilla balance:
* **Line of Sight Required:** Teleport target locations require an unobstructed line of sight from the shulker's position, preventing shulkers from teleporting to outside rooms or through thick walls.
* **Teleport Cooldown:** Adds an internal cooldown between damage-induced panic teleports to reduce endless teleportation loops during combat.
* **Ground Distance Check:** Shulkers prioritize landing on surfaces that have solid ground within 4 blocks underneath, reducing void teleports and unreachable ceiling attachments.

### Player Leashing
Allows players to attach a lead to other players:
* Server-side physics gently guide the leashed player along without rubberbanding or client desynchronization.
* Tethers cleanly snap or detach when exceeding distance limits.
* Good for trolling players, minigames, moving alt-accounts, and more.

### End Dimension Access Control
Server administrators can manage access to the End dimension:
* **End Entry Toggle:** Enable or disable player entry through End Portals. When disabled, portal entry is canceled.
* **Optional Chat Notification:** Configure whether players receive an informative message when attempting to enter a disabled End portal.
* **Eye Tracking:** When disabled, thrown Eyes of Ender fly in randomized 360-degree directions rather than locating Strongholds, allowing progression gating without disabling the End dimension entirely.

### AFK System
* Automatically marks idle players with an `[AFK]` status after 5 minutes (300 seconds) of inactivity.
* Appends `[AFK]` to the player name in the player Tab list and server status multiplayer list hover.
* AFK players are excluded from player sleep percentages so active players can skip the night more easily.

### Name Tag Player Renaming
* Players can rename other players by right-clicking them with a Name Tag.
* Updates the visible name in the Tab list, player overhead nameplate, chat messages, and death messages.
* Renaming is fully persistent across server restarts and player deaths.
* Renaming a player using their original username cleanly reverts their display name back to vanilla.

### Invisible Item Frames
* Sneak + Right-Click any Item Frame containing an item to toggle the frame invisible.
* If the item is removed by any player or projectile, the frame automatically reverts to being visible.
* Good for maparts, item displays, and other decoration.

### Server MOTD Command
* Inspect and modify the multiplayer server list MOTD in real time using `/outbound motd <text>`.
* Supports standard color formatting codes (`&a`, `&6`, `&l`, etc.) and multi-line breaks (`\n`).
* Automatically persists changes to `server.properties` on dedicated servers without requiring a server reboot.

### Farmland and Combat Tweaks
* **Farmland Protection:** Players wearing boots enchanted with Feather Falling will not trample farmland crops when jumping or landing.
* **Snowball Knockback:** Thrown snowballs apply knockback to players, just like how it works in Bedrock Edition.
* **Enderman Griefing Toggle:** Disable Enderman block picking and placing independently from the global `mobGriefing` gamerule.
* **Survival Light Blocks:** Light blocks can be crafted and broken in Survival mode, displaying the light block particle when holding a light block, just like in Creative mode.

---

## Commands

All commands are consolidated under the `/outbound` root command (requires permission level 2 / OP):

| Command | Description |
| :--- | :--- |
| `/outbound` | Displays an overview and status of all features |
| `/outbound reload` | Reloads configuration from disk and reapplies modifiers |
| `/outbound motd [text]` | Views current MOTD or updates it in real time |
| `/outbound allowEnd [bool\|toggle]` | Controls access to the End dimension |
| `/outbound endMessage [bool\|toggle]` | Toggles notification message when End entry is blocked |
| `/outbound allowEyeTracking [bool\|toggle]` | Controls Stronghold tracking for Eyes of Ender |
| `/outbound afk [bool\|toggle]` | Enables or disables the AFK system |
| `/outbound playerNametag [bool\|toggle]` | Enables or disables Name Tag player renaming |
| `/outbound invisibleItemFrames [bool\|toggle]` | Enables or disables invisible Item Frames |
| `/outbound playerLeashing [bool\|toggle]` | Enables or disables player leashing |
| `/outbound snowballKnockback [bool\|toggle]` | Enables or disables snowball player knockback |
| `/outbound featherFallingFarmland [bool\|toggle]` | Enables or disables farmland protection with Feather Falling |
| `/outbound lightBreak [bool\|toggle]` | Enables or disables survival light block breaking |
| `/outbound endermanGrief [bool\|toggle]` | Enables or disables Enderman block griefing |

---

## Configuration (`config/outbound.json`)

```json
{
  "enableCustomPortals": true,
  "allowAnyCustomPortalSize": false,
  "enableClimbingMinecartPickup": true,
  "enableShulkerDamageTeleportAdjustments": true,
  "shulkerTeleportCooldownTicks": 80,
  "shulkerMaxGroundDistance": 4,
  "shulkerTeleportRequiresLineOfSight": true,
  "preventEndermanGrief": true,
  "enableLightBlockSurvivalBreak": true,
  "featherFallingProtectsFarmland": true,
  "allowEndEntry": true,
  "showEndClosedMessage": false,
  "allowEnderEyeTracking": true,
  "enableSnowballPlayerKnockback": true,
  "enablePlayerLeashing": true,
  "playerLeashDistanceMin": 4.0,
  "playerLeashDistanceMax": 10.0,
  "allowLeashedRemoveFenceKnot": false,
  "enableAfkSystem": true,
  "afkTimeoutSeconds": 300,
  "enablePlayerNametagRenaming": true,
  "enableInvisibleItemFrames": true,
  "debugLogging": false
}
```

---

## Building & Testing

Requires Java 25:
```bash
JAVA_HOME=/path/to/jdk-25 ./gradlew build
```
The compiled jar will be available at `build/libs/outbound-1.0.0.jar`.
