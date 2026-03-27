package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

/**
 * Reports success or failure to a client in response to a request to modify a shard.
 */
public record S2CModifyShardResult(ResourceLocation shardId, boolean success) implements CustomPacketPayload {
	public static final Type<S2CModifyShardResult> PACKET_ID = new Type<>(ScatteredShards.id("modify_shard_result"));
	public static final StreamCodec<RegistryFriendlyByteBuf, S2CModifyShardResult> PACKET_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, S2CModifyShardResult::shardId, ByteBufCodecs.BOOL, S2CModifyShardResult::success, S2CModifyShardResult::new);

	@Environment(EnvType.CLIENT)
	public static void receive(S2CModifyShardResult payload, ClientPlayNetworking.Context context) {
		context.client().execute(() -> {
			ScatteredShardsClient.triggerShardModificationToast(payload.shardId(), payload.success());
			context.client().setScreen(null);
		});
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
