package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;

public class ScatteredShardsAPI {
	
	public static final String MODIFY_SHARD_PERMISSION = ScatteredShards.permission("modify_shard");

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

	/**
	 * This just forwards the collect down to the ShardCollectionComponent
	 * @see ShardCollectionComponent#addShard(Identifier)
	 */
	public static void triggerShardCollection(ServerPlayerEntity player, Identifier shardId) {
		ShardCollectionComponent collection = ScatteredShardsComponents.COLLECTION.get(player);
		collection.addShard(shardId);
	}
}
