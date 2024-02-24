# Respawn

Respawn is a mod for minecraft neoforge that allows server ops to set the default spawn/respawn dimension for the server. Built jars are available here:
* https://curseforge.com/minecraft/mc-mods/respawn
* https://modrinth.com/mod/respawn

This mod is fully serversided; clients do not need to install the mod to connect to servers that have the mod, and clients without the mod can use the mod's commands if the server has the mod.

## Commands

Respawn provides two commands for configuring respawn in-game (respawn can also be configured prior to world creation by using config files, see the next section for more information).

### Set Dimension

`/respawn dimension <dimension>`

This command sets the respawn dimension to the specified dimension id.

### Disable

`/respawn disable`

This command disables Respawn's dimension override; players will again respawn in the overworld afterward (this also allows other mods to handle the default spawn behavior without Respawn interfering). Respawn can be re-enabled via `/respawn dimension`.

### Changing the Spawn Position

The spawn position can be configured via vanilla commands and world data (rather than through Respawn):

`/execute in minecraft:overworld run setworldspawn <x> <y> <z>`

Due to the way vanilla implements the respawn position, this *must* be set in the overworld (either via `execute in` or by standing in the overworld before running setworldspawn).

The global spawn position can be configured outside of a running game by editing the level.dat nbt file.

https://minecraft.fandom.com/wiki/Java_Edition_level_format#level.dat_format

## Server Config

A config file for Respawn is generated whenever a new save folder is created, at <savefolder>/serverconfig/respawn-server.toml.

This file can be moved to <minecraftfolder>/defaultconfigs/respawn-server.toml to set default configs for new worlds.

The initial config file is as follows:

```toml
#This serverconfig can be configured in-game by server ops via commands.
#The `/respawn dimension` command will enable the mod and set a respawn dimension.
#The `/respawn disable` command will disable the mod and revert to vanilla spawning behavior.
#
#Whether to override the default spawn dimension.
#Set this to false to use vanilla behavior or allow other mods to override the default spawn dimension.
enabled = false
#Dimension players will respawn in when they have no respawn point set (from bed, anchors, etc).
#This is "minecraft:overworld" by default.
respawnDimension = "minecraft:overworld"
#Minimum permission level to use /respawn commands to alter respawn config in-game.
#Defaults to 2 (same as vanilla /setworldspawn)
#Range: 0 ~ 4
minPermissionLevel = 2
```
