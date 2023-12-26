package net.modfest.scatteredshards.networking;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.impl.ShardCollectionPersistentState;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

public class ScatteredShardsNetworking {
	
	/*--------------------------*
	 * Server to Client packets *
	 *--------------------------*/
	
	/**
	 * Syncs or adds one shard to the client, leaving all others untouched
	 */
	public static class S2CSyncShard {
		public static final Identifier ID = ScatteredShards.id("sync_shard");
		
		public static void send(ServerPlayerEntity player, Identifier shardId, Shard shard) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeIdentifier(shardId);
			buf.writeNbt(shard.toNbt());
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			Identifier shardId = buf.readIdentifier();
			NbtCompound compound = buf.readNbt();
			
			client.execute(() -> {
				Shard shard = Shard.fromNbt(compound);
				
				ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
				if (library != null) library.shards().put(shardId, shard);
			});
		}
	}
	
	/**
	 * Deletes one shard from the client, leaving all others untouched
	 */
	public static class S2CDeleteShard {
		public static final Identifier ID = ScatteredShards.id("delete_shard");
		
		public static void send(ServerPlayerEntity player, Identifier shardId) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeIdentifier(shardId);
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		public static void sendToAll(MinecraftServer server, Identifier shardId) {
			for(var player : server.getPlayerManager().getPlayerList()) {
				send(player, shardId);
			}
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			Identifier shardId = buf.readIdentifier();
			
			client.execute(() -> {
				ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
				if (library != null) library.shards().remove(shardId);
			});
		}
	}
	
	/**
	 * Wipes out the client's recorded shards and shardSets for this session, and replaces them with the supplied information.
	 */
	public static class S2CSyncLibrary {
		public static final Identifier ID = ScatteredShards.id("sync_library");
		
		public static void send(ServerPlayerEntity player) {
			PacketByteBuf buf = PacketByteBufs.create();
			ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
			
			buf.writeNbt(library.shardTypes().toNbt());
			buf.writeNbt(library.shards().toNbt());
			
			buf.writeMap(library.shardSets().asMap(), PacketByteBuf::writeIdentifier, (b, collection) -> {
				b.writeCollection(collection, PacketByteBuf::writeIdentifier);
			});
			
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		public static void sendToAll(MinecraftServer server) {
			for(var player : server.getPlayerManager().getPlayerList()) {
				send(player);
			}
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			ScatteredShards.LOGGER.info("Received sync");
			NbtCompound shardTypeNbt = buf.readNbt();
			NbtCompound shardNbt = buf.readNbt();
			
			ScatteredShards.LOGGER.info("  " + shardTypeNbt + " shardTypes to sync . . .");
			
			Map<Identifier, Set<Identifier>> shardSetMap = buf.readMap(
					PacketByteBuf::readIdentifier,
					(b) -> b.readCollection(HashSet::new, PacketByteBuf::readIdentifier)
					);
			
			client.execute(() -> {
				ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
				
				library.clearAll();
				library.shardTypes().syncFromNbt(shardTypeNbt);
				library.shards().syncFromNbt(shardNbt);
				
				for(Map.Entry<Identifier, Set<Identifier>> entry : shardSetMap.entrySet()) {
					library.shardSets().putAll(entry.getKey(), entry.getValue());
				}
				
				//Tidy up in case MISSING got dropped
				if (library.shardTypes().get(ShardType.MISSING_ID) == null) {
					library.shardTypes().put(ShardType.MISSING_ID, ShardType.MISSING);
				}
			});
		}
	}
	
	public static class S2CSyncCollection {
		public static final Identifier ID = ScatteredShards.id("sync_collection");
		
		public static void send(ServerPlayerEntity player) {
			PacketByteBuf buf = PacketByteBufs.create();
			var collection = ScatteredShardsAPI.getServerCollection(player);
			var imm = collection.toImmutableSet();
			buf.writeCollection(imm, PacketByteBuf::writeIdentifier);
			
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			HashSet<Identifier> data = buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier);
			
			client.execute(() -> {
				ScatteredShards.LOGGER.info("Syncing collection with " + data.size() + " shards.");
				
				var collection = ScatteredShardsAPI.getClientCollection();
				collection.clear();
				collection.addAll(data);
			});
		}
	}
	
	/**
	 * Syncs the addition of a shard to a Player's ShardCollection, and triggers the appropriate animation and toast.
	 */
	public static class S2CCollectShard {
		public static final Identifier ID = ScatteredShards.id("collect_shard");
		
		public static void send(ServerPlayerEntity player, Identifier shardId) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeIdentifier(shardId);
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			final Identifier shardId = buf.readIdentifier();

			client.execute(() -> {
				ScatteredShardsClient.triggerShardCollectAnimation(shardId);
				ScatteredShardsAPI.getClientCollection().add(shardId);
			});
		}
	}
	
	/**
	 * Syncs the removal of a shard from a Player's ShardCollection
	 */
	public static class S2CUncollectShard {
		public static final Identifier ID = ScatteredShards.id("uncollect_shard");
		
		public static void send(ServerPlayerEntity player, Identifier shardId) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeIdentifier(shardId);
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			final Identifier shardId = buf.readIdentifier();

			client.execute(() -> {
				ScatteredShardsAPI.getClientCollection().remove(shardId);
			});
		}
	}
	
	/**
	 * Reports success or failure to a client in response to a request to modify a shard.
	 */
	public static class S2CModifyShardResult {
		public static final Identifier ID = ScatteredShards.id("modify_shard_result");
		
		public static void send(ServerPlayerEntity player, Identifier shardId, boolean success) {
			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeIdentifier(shardId);
			buf.writeBoolean(success);
			ServerPlayNetworking.send(player, ID, buf);
		}
		
		@Environment(EnvType.CLIENT)
		public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
			final Identifier shardId = buf.readIdentifier();
			final boolean success = buf.readBoolean();

			client.execute(() -> {
				ScatteredShardsClient.triggerShardModificationToast(shardId, success);
			});
		}
	}
	
	
	/*--------------------------*
	 * Client to Server packets *
	 *--------------------------*/
	
	/**
	 * Requests that a shard be created or modified. Requires permissions!
	 */
	public static class C2SModifyShard {
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
					
					//Make sure the NBT gets written on next world-save
					ShardLibraryPersistentState.get(server).markDirty();
					
					//Update everyone's client libraries with the new shard
					for(ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
						ScatteredShardsNetworking.S2CSyncShard.send(otherPlayer, shardId, shard);
					}
				}
			});
		}
	}
	

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncShard.ID, S2CSyncShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CDeleteShard.ID, S2CDeleteShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncLibrary.ID, S2CSyncLibrary::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncCollection.ID, S2CSyncCollection::receive);
		
		ClientPlayNetworking.registerGlobalReceiver(S2CCollectShard.ID, S2CCollectShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CUncollectShard.ID, S2CUncollectShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CModifyShardResult.ID, S2CModifyShardResult::receive);
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(C2SModifyShard.ID, C2SModifyShard::receive);
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ShardLibraryPersistentState.get(server); // Trigger the PersistentState load if it hasn't yet
			S2CSyncLibrary.send(handler.getPlayer());
			ShardCollectionPersistentState.get(server); // Trigger the PersistentState load if it hasn't yet
			S2CSyncCollection.send(handler.getPlayer());
		});
		
	}
}
