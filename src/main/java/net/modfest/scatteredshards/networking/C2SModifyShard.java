package net.modfest.scatteredshards.networking;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Requests that a shard be created or modified. Requires permissions!
 */
public class C2SModifyShard {
	public static final Identifier ID = ScatteredShards.id("modify_shard");
	
	@Environment(EnvType.CLIENT)
	public static void send(Identifier shardId, Shard shard) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIdentifier(shardId);
		buf.writeNbt(shard.toNbt());
		ClientPlayNetworking.send(ID, buf);
	}
	
	public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		final Identifier shardId = buf.readIdentifier();
		final NbtCompound shardNbt = buf.readNbt();
		
		server.execute(() -> {
			boolean success = Permissions.check(player, ScatteredShardsAPI.MODIFY_SHARD_PERMISSION, 1);
			
			final Shard shard = Shard.fromNbt(shardNbt);
			
			//Let the sender know of success or failure before a shard update comes through
			S2CModifyShardResult.send(player, shardId, success);
			
			if (success) {
				//Update our serverside library
				ScatteredShardsAPI.getServerLibrary().shards().put(shardId, shard);
				ScatteredShardsAPI.getServerLibrary().shardSets().put(shard.sourceId(), shardId);
				
				//Make sure the NBT gets written on next world-save
				ShardLibraryPersistentState.get(server).markDirty();
				
				//Update everyone's client libraries with the new shard
				for(ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
					S2CSyncShard.send(otherPlayer, shardId, shard);
				}
			}
		});
	}
}