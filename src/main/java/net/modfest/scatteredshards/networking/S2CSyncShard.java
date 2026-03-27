package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Syncs or adds one shard to the client, leaving all others untouched
 */
public record S2CSyncShard(ResourceLocation shardId, Shard shard) implements CustomPacketPayload {
	public static final Type<S2CSyncShard> PACKET_ID = new Type<>(ScatteredShards.id("sync_shard"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncShard> PACKET_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, S2CSyncShard::shardId, Shard.PACKET_CODEC, S2CSyncShard::shard, S2CSyncShard::new);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncShard payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			library.shards().put(payload.shardId(), payload.shard());
			library.shardSets().put(payload.shard().sourceId(), payload.shardId());
			//ScatteredShards.LOGGER.info("Updated data for shard \"" + shardId + "\"");
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
