package net.modfest.scatteredshards.api.impl;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.ShardSet;
import net.modfest.scatteredshards.load.ShardSetLoader;

import java.util.HashMap;
import java.util.Map;

public class ScatteredShardsAPIImpl {

	private static final Map<Identifier, ShardSet> REGISTERED_SHARD_SETS = new HashMap<>();
	private static Map<Identifier, ShardSet> shardSetMap = null;

	public static Map<Identifier, ShardSet> getOrCreateShardSets() {
		if (shardSetMap != null) {
			return shardSetMap;
		}
		shardSetMap = new HashMap<>(REGISTERED_SHARD_SETS);
		shardSetMap.putAll(ShardSetLoader.LOADED_SHARD_SETS);
		return shardSetMap;
	}
}
