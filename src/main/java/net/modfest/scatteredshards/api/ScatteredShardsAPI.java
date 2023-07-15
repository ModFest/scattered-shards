package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.core.api.shard.ShardType;

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
}
