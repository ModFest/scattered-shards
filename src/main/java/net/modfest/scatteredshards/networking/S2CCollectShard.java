package net.modfest.scatteredshards.networking;

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
import net.modfest.scatteredshards.client.ScatteredShardsClient;

/**
 * Syncs the addition of a shard to a Player's ShardCollection, and triggers the appropriate animation and toast.
 */
public class S2CCollectShard {
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