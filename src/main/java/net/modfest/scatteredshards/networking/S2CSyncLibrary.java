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
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.ShardType;

/**
 * Wipes out the client's recorded shards and shardSets for this session, and replaces them with the supplied information.
 */
public record S2CSyncLibrary(ShardLibrary library) implements CustomPacketPayload {
	public static final Type<S2CSyncLibrary> PACKET_ID = new Type<>(ScatteredShards.id("sync_library"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncLibrary> PACKET_CODEC = ShardLibrary.PACKET_CODEC.map(S2CSyncLibrary::new, S2CSyncLibrary::library).cast();

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncLibrary payload, ClientPlayNetworking.Context context) {
		ScatteredShards.LOGGER.info("Syncing ShardLibrary...");

		context.client().execute(() -> {
			ScatteredShardsAPI.updateClientShardLibrary(payload.library());
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();

			//Tidy up in case MISSING got dropped
			if (library.shardTypes().get(ShardType.MISSING_ID).isEmpty()) {
				library.shardTypes().put(ShardType.MISSING_ID, ShardType.MISSING);
			}

			ScatteredShards.LOGGER.info("Sync complete. ShardLibrary has {} shard types, {} shards, and {} shardSets.", library.shardTypes().size(), library.shards().size(), library.shardSets().size());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
