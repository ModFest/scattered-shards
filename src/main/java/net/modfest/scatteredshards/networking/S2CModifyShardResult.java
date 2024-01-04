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
import net.modfest.scatteredshards.client.ScatteredShardsClient;

/**
 * Reports success or failure to a client in response to a request to modify a shard.
 */
public class S2CModifyShardResult {
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
			client.setScreen(null);
		});
	}
}