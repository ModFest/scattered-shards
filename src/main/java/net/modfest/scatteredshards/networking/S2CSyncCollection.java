package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;

public record S2CSyncCollection(ShardCollection collection) implements CustomPacketPayload {
	public static final Type<S2CSyncCollection> PACKET_ID = new Type<>(ScatteredShards.id("sync_collection"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncCollection> PACKET_CODEC = ShardCollection.PACKET_CODEC.map(S2CSyncCollection::new, S2CSyncCollection::collection);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncCollection payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ScatteredShards.LOGGER.info("Syncing ShardCollection with {} shards collected.", payload.collection().size());
			ScatteredShardsAPI.updateClientShardCollection(payload.collection());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
