package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.GlobalCollection;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record S2CSyncGlobalCollection(GlobalCollection globalCollection) implements CustomPacketPayload {
	public static final Type<S2CSyncGlobalCollection> PACKET_ID = new Type<>(ScatteredShards.id("sync_global_collection"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncGlobalCollection> PACKET_CODEC = GlobalCollection.PACKET_CODEC.map(S2CSyncGlobalCollection::new, S2CSyncGlobalCollection::globalCollection).cast();

	@Environment(EnvType.CLIENT)
	public static void receive(S2CSyncGlobalCollection payload, ClientPlayNetworking.Context context) {
		ScatteredShards.LOGGER.info("Syncing GlobalShardCollection...");

		context.client().execute(() -> {
			ScatteredShardsAPI.updateClientGlobalCollection(payload.globalCollection());
			ScatteredShards.LOGGER.info("Sync complete. Received data for {} players.", payload.globalCollection.totalPlayers());
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
