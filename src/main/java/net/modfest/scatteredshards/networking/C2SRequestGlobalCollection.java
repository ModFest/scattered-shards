package net.modfest.scatteredshards.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class C2SRequestGlobalCollection implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<C2SRequestGlobalCollection> PACKET_ID = new CustomPacketPayload.Type<>(ScatteredShards.id("request_global_collection"));
	public static final C2SRequestGlobalCollection INSTANCE = new C2SRequestGlobalCollection();
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SRequestGlobalCollection> PACKET_CODEC = StreamCodec.unit(INSTANCE);

	public static void receive(C2SRequestGlobalCollection payload, ServerPlayNetworking.Context context) {
		ServerPlayNetworking.send(context.player(), new S2CSyncGlobalCollection(ScatteredShardsAPI.getServerGlobalCollection()));
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
