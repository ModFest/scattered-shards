package net.modfest.scatteredshards.api.impl;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.load.ShardSetLoader;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

public class ScatteredShardsAPIImpl {

	private static final Multimap<Identifier, Shard> REGISTERED_SHARD_SETS = MultimapBuilder.hashKeys().arrayListValues(3).build();
	private static final BiMap<Identifier, Shard> REGISTERED_SHARDS = HashBiMap.create();
	private static Multimap<Identifier, Shard> shardSetCache = null;
	private static BiMap<Identifier, Shard> shardCache = null;

	public static Multimap<Identifier, Shard> getOrCreateShardSets() {
		if (shardSetCache == null) {
			shardSetCache = MultimapBuilder.hashKeys().arrayListValues(3).build();
			shardSetCache.putAll(ShardSetLoader.BY_SHARD_SET);
			shardSetCache.putAll(REGISTERED_SHARD_SETS);
		}
		
		return shardSetCache;
	}

	public static BiMap<Identifier, Shard> getOrCreateShardData() {
		if (shardCache == null) {
			shardCache = HashBiMap.create();
			shardCache.putAll(ShardSetLoader.BY_ID);
			shardCache.putAll(REGISTERED_SHARDS);
		}
		
		return shardCache;
	}

	public static void onReload() {
		shardSetCache = null;
		shardCache = null;
	}
}
