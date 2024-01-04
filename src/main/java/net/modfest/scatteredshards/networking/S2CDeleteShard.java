package net.modfest.scatteredshards.networking;

import java.util.Optional;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Deletes one shard from the client, leaving all others untouched
 */
public class S2CDeleteShard {
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
			Optional<Shard> shard = library.shards().get(shardId);
			library.shards().remove(shardId);
			shard.ifPresent(it -> {
				library.shardSets().remove(it.sourceId(), shardId);
			});
		});
	}
}