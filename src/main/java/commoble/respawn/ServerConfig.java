package commoble.respawn;

import commoble.respawn.ConfigHelper.ConfigObject;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.common.ModConfigSpec.BooleanValue;
import net.neoforged.neoforge.common.ModConfigSpec.IntValue;

public record ServerConfig(BooleanValue enabled, ConfigObject<ResourceKey<Level>> respawnDimension, IntValue minPermissionLevel)
{
	public static ServerConfig create(ModConfigSpec.Builder builder)
	{
		builder.comment(
			"This serverconfig can be configured in-game by server ops via commands.",
			"The `/respawn dimension` command will enable the mod and set a respawn dimension.",
			"The `/respawn disable` command will disable the mod and revert to vanilla spawning behavior.",
			"");
		
		builder.comment(
			"Whether to override the default spawn dimension.",
			"Set this to false to use vanilla behavior or allow other mods to override the default spawn dimension.");
		var enabled = builder.define("enabled", false);
		
		builder.comment(
			"Dimension players will respawn in when they have no respawn point set (from bed, anchors, etc).",
			"This is \"minecraft:overworld\" by default.");
		var respawnDimension = ConfigHelper.defineObject(builder, "respawnDimension", ResourceKey.codec(Registries.DIMENSION), Level.OVERWORLD);
		
		builder.comment(
			"Minimum permission level to use /respawn commands to alter respawn config in-game.",
			"Defaults to 2 (same as vanilla /setworldspawn)");
		var minPermissionLevel = builder.defineInRange("minPermissionLevel", 2, 0, 4);
		
		return new ServerConfig(enabled, respawnDimension, minPermissionLevel);
	}
}
