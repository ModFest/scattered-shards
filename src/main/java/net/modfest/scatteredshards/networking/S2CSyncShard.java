package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Syncs or adds one shard to the client, leaving all others untouched
 */
public class S2CSyncShard {
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
			ScatteredShards.LOGGER.info("Received SyncShard for " + shardId);
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			library.shards().put(shardId, shard);
			library.shardSets().put(shard.sourceId(), shardId);
		});
	}
}