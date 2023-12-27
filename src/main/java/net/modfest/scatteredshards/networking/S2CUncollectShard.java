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

/**
 * Syncs the removal of a shard from a Player's ShardCollection
 */
public class S2CUncollectShard {
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