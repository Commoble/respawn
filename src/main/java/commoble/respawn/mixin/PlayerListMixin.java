package commoble.respawn.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import commoble.respawn.RespawnMod;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.ProfilePublicKey;

@Mixin(PlayerList.class)
public class PlayerListMixin
{
	@SuppressWarnings("rawtypes")
	@Redirect(method="placeNewPlayer", allow=2, require=2, at = @At(value="FIELD", target="Lnet/minecraft/world/level/Level;OVERWORLD:Lnet/minecraft/resources/ResourceKey;"))
	private ResourceKey redirectPlayerListPlaceNewPlayerGetOverworld()
	{
		return RespawnMod.instance().redirectPlayerListPlaceNewPlayerGetOverworld();
	}
	
	@Inject(method="getPlayerForLogin", at=@At("TAIL"), cancellable=true)
	private void onPlayerListGetPlayerForLogin(GameProfile profile, @Nullable ProfilePublicKey key, CallbackInfoReturnable<ServerPlayer> cir)
	{
		RespawnMod.instance().onPlayerListGetPlayerForLogin((PlayerList)(Object)this, profile, key, cir);
	}
	
	@Redirect(method="respawn", at=@At(value="INVOKE", target="Lnet/minecraft/server/MinecraftServer;overworld()Lnet/minecraft/server/level/ServerLevel;"))
	private ServerLevel redirectPlayerListRespawnGetServerOverworld(MinecraftServer server)
	{
		return RespawnMod.instance().redirectPlayerListRespawnGetServerOverworld(server);
	}
}
