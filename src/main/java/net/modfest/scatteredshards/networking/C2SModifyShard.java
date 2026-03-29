package net.modfest.scatteredshards.networking;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload.Type;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Requests that a shard be created or modified. Requires permissions!
 */
public record C2SModifyShard(Identifier shardId, Shard shard) implements CustomPacketPayload {
	public static final Type<C2SModifyShard> PACKET_ID = new Type<>(ScatteredShards.id("modify_shard"));
	public static final StreamCodec<RegistryFriendlyByteBuf, C2SModifyShard> PACKET_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, C2SModifyShard::shardId, Shard.PACKET_CODEC, C2SModifyShard::shard, C2SModifyShard::new);

	public static void receive(C2SModifyShard payload, ServerPlayNetworking.Context context) {
		modify(context.player(), payload.shardId, payload.shard);
	}

	public static boolean modify(ServerPlayer player, Identifier shardId, Shard shard) {
		MinecraftServer server = player.level().getServer();
		assert server != null;

		boolean success = server.isSingleplayer() || Permissions.check(player, ScatteredShardsAPI.MODIFY_SHARD_PERMISSION, 1);

		server.execute(() -> {
			//Let the sender know of success or failure before a shard update comes through
			ServerPlayNetworking.send(player, new S2CModifyShardResult(shardId, success));

			if (!success) {
				return;
			}

			//Update our serverside library
			ScatteredShardsAPI.getServerLibrary().shards().put(shardId, shard);
			ScatteredShardsAPI.getServerLibrary().shardSets().put(shard.sourceId(), shardId);

			//Make sure the NBT gets written on next world-save
			ShardLibraryPersistentState.get(server).setDirty();

			//Update everyone's client libraries with the new shard
			S2CSyncShard syncShard = new S2CSyncShard(shardId, shard);
			for (ServerPlayer otherPlayer : server.getPlayerList().getPlayers()) {
				ServerPlayNetworking.send(otherPlayer, syncShard);
			}
		});

		return success;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return PACKET_ID;
	}
}
