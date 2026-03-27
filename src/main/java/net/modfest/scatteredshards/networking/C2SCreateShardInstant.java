package net.modfest.scatteredshards.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.block.ShardBlock;

/**
 * Requests that a shard be created. Requires permissions!
 */
public record C2SCreateShardInstant(ResourceLocation shardId, Shard shard) implements CustomPacketPayload {
	public static final Type<C2SCreateShardInstant> PACKET_ID = new Type<>(ScatteredShards.id("create_shard_instant"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SCreateShardInstant> PACKET_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC, C2SCreateShardInstant::shardId, Shard.PACKET_CODEC, C2SCreateShardInstant::shard, C2SCreateShardInstant::new);

	public static void receive(C2SCreateShardInstant payload, ServerPlayNetworking.Context context) {
		boolean success = C2SModifyShard.modify(context.player(), payload.shardId, payload.shard);

		if (!success) {
			return;
		}

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();

		ItemStack itemStack = ShardBlock.createShardBlock(library, payload.shardId(), false, 0.5f, 0.5f);
		context.player().getInventory().placeItemBackInInventory(itemStack);
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
