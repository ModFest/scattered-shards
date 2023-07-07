package net.modfest.scatteredshards.api.impl;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.ShardData;
import net.modfest.scatteredshards.core.api.shard.ShardSet;
import net.modfest.scatteredshards.load.ShardSetLoader;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ScatteredShardsAPIImpl {

	private static final Map<Identifier, ShardSet> REGISTERED_SHARD_SETS = new HashMap<>();
	private static Map<Identifier, ShardSet> shardSetMap = null;
	private static Map<Identifier, ShardData> shardDataMap = null;

	public static Map<Identifier, ShardSet> getOrCreateShardSets() {
		if (shardSetMap != null) {
			return shardSetMap;
		}
		shardSetMap = new HashMap<>(REGISTERED_SHARD_SETS);
		shardSetMap.putAll(ShardSetLoader.LOADED_SHARD_SETS);
		return shardSetMap;
	}

	public static Map<Identifier, ShardData> getOrCreateShardData() {
		if (shardDataMap != null) {
			return shardDataMap;
		}
		shardDataMap = shardSetMap.values().stream()
				.flatMap(set -> set.getShards().stream())
				.collect(Collectors.toMap(ShardData::id, shard -> shard));
		return shardDataMap;
	}

	public static void onReload() {
		shardSetMap = null;
		shardDataMap = null;
	}
}
