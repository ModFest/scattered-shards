package net.modfest.scatteredshards.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.block.ShardBlock;

/**
 * Requests that a shard be created. Requires permissions!
 */
public record C2SCreateShardInstant(Identifier shardId, Shard shard) implements CustomPayload {
	public static final Id<C2SCreateShardInstant> PACKET_ID = new Id<>(ScatteredShards.id("create_shard_instant"));
	public static final PacketCodec<RegistryByteBuf, C2SCreateShardInstant> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, C2SCreateShardInstant::shardId, Shard.PACKET_CODEC, C2SCreateShardInstant::shard, C2SCreateShardInstant::new);

	public static void receive(C2SCreateShardInstant payload, ServerPlayNetworking.Context context) {
		boolean success = C2SModifyShard.modify(context.player(), payload.shardId, payload.shard);

		if (!success) {
			return;
		}

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();

		ItemStack itemStack = ShardBlock.createShardBlock(library, payload.shardId(), false, 0.5f, 0.5f);
		context.player().getInventory().offerOrDrop(itemStack);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
