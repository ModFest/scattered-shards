package net.modfest.scatteredshards.networking;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.block.ShardBlock;

/**
 * Requests that a shard be created. Requires permissions!
 */
public record C2SCreateShardInstant(Identifier shardId, Shard shard) implements CustomPayload {
	public static final Id<C2SCreateShardInstant> PACKET_ID = new Id<>(ScatteredShards.id("create_shard_instant"));
	public static final PacketCodec<RegistryByteBuf, C2SCreateShardInstant> PACKET_CODEC = PacketCodec.tuple(Identifier.PACKET_CODEC, C2SCreateShardInstant::shardId, Shard.PACKET_CODEC, C2SCreateShardInstant::shard, C2SCreateShardInstant::new);

	public static void receive(C2SCreateShardInstant payload, ServerPlayNetworking.Context context) {
		// TODO: This is mostly identical to C2SModifyShard, no need for so much duplicate code

		MinecraftServer server = context.player().getServer();
		server.execute(() -> {
			boolean success = server.isSingleplayer() || Permissions.check(context.player(), ScatteredShardsAPI.MODIFY_SHARD_PERMISSION, 1);

			//Let the sender know of success or failure before a shard update comes through
			ServerPlayNetworking.send(context.player(), new S2CModifyShardResult(payload.shardId(), success));

			if (!success) {
				return;
			}

			//Update our serverside library
			var library = ScatteredShardsAPI.getServerLibrary();
			library.shards().put(payload.shardId(), payload.shard());
			library.shardSets().put(payload.shard().sourceId(), payload.shardId());

			//Make sure the NBT gets written on next world-save
			ShardLibraryPersistentState.get(server).markDirty();

			//Update everyone's client libraries with the new shard
			S2CSyncShard syncShard = new S2CSyncShard(payload.shardId(), payload.shard());
			for (ServerPlayerEntity otherPlayer : server.getPlayerManager().getPlayerList()) {
				ServerPlayNetworking.send(otherPlayer, syncShard);
			}

			var itemStack = ShardBlock.createShardBlock(library, payload.shardId(), false, 0.5f, 0.5f);
			context.player().getInventory().offerOrDrop(itemStack);
		});
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return PACKET_ID;
	}
}
