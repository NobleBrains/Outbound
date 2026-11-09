# Outbound 

A purely server-side Fabric mod adding vanilla-faithful enhancements. Unmodded vanilla clients can connect with zero client-side mods required.

---

## Features

### 1. Compact & Custom Nether Portals
In vanilla Minecraft, Nether Portals require an obsidian frame opening of at least 2x3 blocks up to 21x21 blocks. **Outbound** allows smaller and compact portals to be ignited using flint & steel or fire charges like any normal portal:
- **1x1 Portals**: A single obsidian ring opening (1 block opening). Perfect for crawl-in portals, hidden bases, redstone contraptions, or item/mob transit.
- **1x2 Portals (Vertical)**: A door-sized opening (1 block wide, 2 blocks high). Convenient for compact pathways and aesthetic builds.
- **2x2 Portals**: A square opening (2 blocks wide, 2 blocks high).
- **Vanilla Compatibility**: Standard 2x3 through 21x21 portals continue to function normally. Corner blocks remain optional. Fully functional on pure vanilla clients with zero client-side mods.

---

### 2. Climbing Entity Minecart Pickup (Cramming Decoupled)
In vanilla Minecraft, `LivingEntity.isPushable()` always returns `false` if an entity is on a climbable (`onClimbable()`: ladders, vines, weeping vines, twisting vines, scaffolding, cave vines, and trapdoors above ladders) to prevent entity cramming.
However, vanilla minecart entry also checks `isPushable()`, preventing minecarts from picking up climbing mobs.
**Outbound** cleanly decouples minecart entry from entity cramming:
- **Entity Cramming Immunity Preserved**: Mob-to-mob collision and cramming checks (`LivingEntity.pushEntities()`) continue to use the standard vanilla pushable logic.
- **Minecart Pickup Enabled**: When a moving, rideable `AbstractMinecart` collides with a living entity on a climbable, the entity can enter the minecart.
- **Supports All Climbables**: Works seamlessly with ladders, vines, scaffolding, weeping vines, twisting vines, and cave vines.
- **Both Movement Engines Supported**: Works with classic vanilla and experimental New Minecart Movement.

---

### 3. Shulker Teleportation Adjustments
In vanilla Minecraft, Shulkers frequently teleport away when damaged below 50% health, often clipping outside through 1-block thick walls, high up on unreachable ceilings, or floating over the void.
**Outbound** refines damage-induced teleportation while keeping it feeling completely vanilla:
- **Same-Room Raytracing / Line of Sight**: Candidate teleport destinations require an unobstructed line of sight (`shulkerTeleportRequiresLineOfSight`, default: `true`). Solid walls, ceilings, and floors block teleports.
- **Teleport Cooldown**: Damage-induced teleports have an internal cooldown (`shulkerTeleportCooldownTicks`, default: `80` ticks / 4 seconds).
- **Accessible Ground Guarantee**: When teleporting due to combat damage, Shulkers only choose positions with solid ground within a maximum of 4 air blocks underneath (`shulkerMaxGroundDistance`, default: `4`).

---

### 4. Player Leashing
Allows players to leash other players using standard vanilla Leads, fully server-side:
- Lead physics smoothly tugs players along without jitter.
- Tethers cleanly detach or snap at distance limits.

---

### 5. End Access & Ender Eye Tracking
- **End Portal Access Toggle**: Control whether the End dimension can be entered.
- **Ender Eye Tracking Toggle**: When disabled, thrown Eyes of Ender fly in random 360° directions instead of locating Strongholds.

---

### 6. AFK System
- Players idle for 5 minutes (default: 300s) automatically enter AFK mode silently.
- Appends ` [AFK]` to the player name in the Tab list and multiplayer server status hover.
- AFK players are excluded from sleeping requirements so sleeping players aren't held back.

---

### 7. Player Name Tag Renaming
- Players can rename other players using an anvil-named Name Tag.
- Changes the visible name in the Tab list, above the head, in chat, and in death messages.
- Persistent across restarts and deaths. Renaming with the player's original name reverts back to normal.

---

### 8. Invisible Item Frames
- Sneak + Right-Click an Item Frame (with an item inside) to make the frame invisible.
- Automatically becomes visible again when the item is removed.

---

### 9. MOTD Command
- Change or inspect the server MOTD dynamically in real time without restarting (`/outbound motd <text>`).
- Supports color codes (`&a`, `&6`, etc.) and linebreaks (`\n`).
- Automatically saved to `server.properties` on Dedicated Servers.

---

## Commands

All commands are exclusively registered under `/outbound` (OP permission level 2 required):

- `/outbound`: Display a full status and subcommand overview of all features.
- `/outbound reload`: Reload configuration from disk and reapply active modifiers.
- `/outbound motd [text]`: View current MOTD, or dynamically update the server MOTD in real time (supports `&` color codes and `\n` linebreaks).
- `/outbound allowEnd [true|false|toggle]`: View, set, or toggle End portal entry permission.
- `/outbound endMessage [true|false|toggle]`: View, set, or toggle End closed chat notification.
- `/outbound allowEyeTracking [true|false|toggle]`: View, set, or toggle Ender Eye Stronghold tracking.
- `/outbound afk [true|false|toggle]`: View, set, or toggle the AFK system.
- `/outbound playerNametag [true|false|toggle]`: View, set, or toggle player Name Tag renaming.
- `/outbound invisibleItemFrames [true|false|toggle]`: View, set, or toggle Shift+Right-Click invisible Item Frames.
- `/outbound playerLeashing [true|false|toggle]`: View, set, or toggle player leashing with leads.
- `/outbound snowballKnockback [true|false|toggle]`: View, set, or toggle snowball knockback on players.
- `/outbound featherFallingFarmland [true|false|toggle]`: View, set, or toggle farmland protection with Feather Falling boots.
- `/outbound lightBreak [true|false|toggle]`: View, set, or toggle survival light block breaking and particle markers.
- `/outbound endermanGrief [true|false|toggle]`: View, set, or toggle Enderman block griefing prevention.

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
