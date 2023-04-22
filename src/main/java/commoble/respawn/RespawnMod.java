package commoble.respawn;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import commoble.databuddy.config.ConfigHelper;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(RespawnMod.MODID)
public class RespawnMod
{
    public static final String MODID = "respawn";
    public static final Logger LOGGER = LogManager.getLogger();
    
    private static final String DIMENSION_ARG = "dimension";
    private static final Collection<String> DIMENSION_EXAMPLES = List.of("minecraft:overworld", "minecraft:nether", "minecraft:end");
    
    private static RespawnMod instance;
    public static RespawnMod instance() { return instance; }
    
    private final ServerConfig serverConfig;
    
    public RespawnMod()
    {
    	instance = this;
    	
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        
        this.serverConfig = ConfigHelper.register(ModConfig.Type.SERVER, ServerConfig::create);
        
        forgeBus.addListener(this::onRegisterCommands);
    }
    
    
    /* Event Listeners */
    
    private void onRegisterCommands(RegisterCommandsEvent event)
    {
    	event.getDispatcher().register(Commands.literal("respawn")
    		.requires(stack -> stack.hasPermission(this.serverConfig.minPermissionLevel().get()))
    		// /respawn dimension <dimension>
    		.then(Commands.literal("dimension")
    			.then(Commands.argument(DIMENSION_ARG, DimensionArgument.dimension())
    				.executes(this::setDimension)))
    		// /respawn disable
    		.then(Commands.literal("disable")
    			.executes(this::disable)));
    }
    
    
    /* Command Handlers */
    
    private int setDimension(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
    	var levelKey = DimensionArgument.getDimension(context, DIMENSION_ARG).dimension();
    	this.serverConfig.enabled().set(true);
    	this.serverConfig.enabled().save();
    	this.serverConfig.respawnDimension().set(levelKey);
    	
    	context.getSource().sendSuccess(Component.literal("Set default respawn dimension to " + levelKey.location()), true);
    	
    	return 1;
    }
    
    private int disable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException
    {
    	this.serverConfig.enabled().set(false);
    	this.serverConfig.enabled().save();
    	this.serverConfig.respawnDimension().set(Level.OVERWORLD);
    	
    	context.getSource().sendSuccess(Component.literal("Disabled respawn dimension override, reverting to vanilla defaults"), true);
    	
    	return 1;
    }
    
    
    /* Mixin Callbacks */

	public ResourceKey<Level> redirectPlayerListPlaceNewPlayerGetOverworld()
	{
		return serverConfig.enabled().get() ? serverConfig.respawnDimension().get() : Level.OVERWORLD;
	}
	
	public void onPlayerListGetPlayerForLogin(PlayerList playerList, GameProfile profile, @Nullable ProfilePublicKey key, CallbackInfoReturnable<ServerPlayer> cir)
	{
		if (serverConfig.enabled().get())
		{
			MinecraftServer server = playerList.getServer();
			var levelKey = serverConfig.respawnDimension().get();
			ServerLevel serverLevel = server.getLevel(levelKey);
			if (serverLevel == null)
			{
				LOGGER.error("Invalid level key {}, please reconfigure your respawn mod via savefolder/serverconfig/respawn-server.toml", levelKey.location());
			}
			else
			{
				cir.setReturnValue(new ServerPlayer(server, serverLevel, profile, key));
			}
		}
	}


	public void onServerPlayerGetRespawnDimension(ServerPlayer serverPlayer, CallbackInfoReturnable<ResourceKey> cir)
	{
		// Player spawn positions can be in one of two states:
		// 1) player has no set spawn, respawn position is null and spawn dimension is overworld
		// 2) player has a set spawn, respawn position is not null and spawn dimension may or may not be overworld
		// so we can check the player's spawn position to see if the server intends to use the "default" spawn dimension (and use our config's override instead)
		if (serverConfig.enabled().get() && serverPlayer.getRespawnPosition() == null)
		{
			cir.setReturnValue(serverConfig.respawnDimension().get());
		}
	}


	public ServerLevel redirectPlayerListRespawnGetServerOverworld(MinecraftServer server)
	{
		if (serverConfig.enabled().get())
		{
			var levelKey = serverConfig.respawnDimension().get();
			ServerLevel serverLevel = server.getLevel(levelKey);
			if (serverLevel == null)
			{
				LOGGER.error("Invalid level key {}, please reconfigure your respawn mod via savefolder/serverconfig/respawn-server.toml", levelKey.location());
			}
			else
			{
				return serverLevel;
			}
		}
		return server.overworld(); // if we don't want to redirect, fall back to vanilla
	}
}
