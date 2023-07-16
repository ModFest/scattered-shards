package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.core.api.shard.ShardType;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class ScatteredShardsAPI {

	public static Multimap<Identifier, Shard> getShardSets() {
		return ScatteredShardsAPIImpl.shardSets;
	}

	public static BiMap<Identifier, Shard> getShardData() {
		return ScatteredShardsAPIImpl.shardData;
	}

	public static BiMap<Identifier, ShardType> getShardTypes() {
		return ScatteredShardsAPIImpl.shardTypes;
	}

	public static void registerShardType(Identifier id, ShardType shardType) {
		ScatteredShardsAPIImpl.REGISTERED_SHARD_TYPES.put(id, shardType);
	}

	public static void triggerShardCollection(ServerPlayerEntity player, Identifier shardId) {
		Shard shard = getShardData().get(shardId);
		if (shard == null) {
			ScatteredShards.LOGGER.warn("Tried to trigger a shard collection for shard '" + shardId.toString() + "' which does not exist.", new IllegalArgumentException()); //Gets us a stack trace for the culprit
			return;
		}

		//TODO: Log this in the player's shard inventory serverside
		ShardEvents.COLLECT.invoker().handle(player, shardId, shard);
		ScatteredShardsNetworking.s2cCollectShard(player, shardId); // This will update the clientside shard binder as well
	}
}
