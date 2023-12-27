package net.modfest.scatteredshards.networking;

import java.util.HashSet;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class S2CSyncCollection {
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
			ScatteredShards.LOGGER.info("Syncing ShardCollection with " + data.size() + " shards collected.");
			
			var collection = ScatteredShardsAPI.getClientCollection();
			collection.clear();
			collection.addAll(data);
		});
	}
}