package net.modfest.scatteredshards.networking;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.ShardType;

/**
 * Wipes out the client's recorded shards and shardSets for this session, and replaces them with the supplied information.
 */
public class S2CSyncLibrary {
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
		ScatteredShards.LOGGER.info("Syncing ShardLibrary...");
		NbtCompound shardTypeNbt = buf.readNbt();
		NbtCompound shardNbt = buf.readNbt();
		
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
			
			ScatteredShards.LOGGER.info("Sync complete. ShardLibrary has " + library.shardTypes().size() + " shard types, " + library.shards().size() + " shards, and " + library.shardSets().size() + " shardSets.");
		});
	}
}