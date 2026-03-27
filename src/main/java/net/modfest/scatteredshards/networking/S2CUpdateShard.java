package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

import java.util.Optional;

/**
 * Collects/uncollects/deletes a shard.
 */
public record S2CUpdateShard(Identifier shardId, Mode mode) implements CustomPacketPayload {
	public static final Type<S2CUpdateShard> PACKET_ID = new Type<>(ScatteredShards.id("update_shard"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CUpdateShard> PACKET_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, S2CUpdateShard::shardId, Mode.PACKET_CODEC, S2CUpdateShard::mode, S2CUpdateShard::new);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CUpdateShard payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			switch (payload.mode()) {
				case COLLECT -> {
					ScatteredShardsClient.onShardCollected(payload.shardId());
					ScatteredShardsAPI.getClientCollection().add(payload.shardId());
				}
				case UNCOLLECT -> ScatteredShardsAPI.getClientCollection().remove(payload.shardId());
				case DELETE -> {
					ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
					Optional<Shard> shard = library.shards().get(payload.shardId());
					library.shards().remove(payload.shardId());
					shard.ifPresent(it -> library.shardSets().remove(it.sourceId(), payload.shardId()));
				}
			}
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}

	public enum Mode {
		COLLECT,
		UNCOLLECT,
		DELETE;

		public static final StreamCodec<RegistryFriendlyByteBuf, Mode> PACKET_CODEC = ByteBufCodecs.INT.map(val -> Mode.values()[val], Mode::ordinal).cast();
	}
}
