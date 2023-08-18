package net.modfest.scatteredshards.api.impl;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.load.ShardTypeLoader;

public class ScatteredShardsAPIImpl {

	public static final Multimap<Identifier, Shard> REGISTERED_SHARD_SETS = MultimapBuilder.hashKeys().arrayListValues(3).build();
	public static final BiMap<Identifier, Shard> REGISTERED_SHARDS = HashBiMap.create();
	public static Multimap<Identifier, Shard> shardSets = null;
	public static BiMap<Identifier, Shard> shardData = null;

	public static final BiMap<Identifier, ShardType> REGISTERED_SHARD_TYPES = HashBiMap.create();
	public static BiMap<Identifier, ShardType> shardTypes = null;

	static {
		updateShards();
		updateShardTypes();
	}

	private static Multimap<Identifier, Shard> createShardSets() {
		Multimap<Identifier, Shard> multimap = MultimapBuilder.hashKeys().arrayListValues(3).build();
		multimap.putAll(ShardSetLoader.BY_SHARD_SET);
		multimap.putAll(REGISTERED_SHARD_SETS);
		return multimap;
	}

	private static BiMap<Identifier, Shard> createShardData() {
		BiMap<Identifier, Shard> map = HashBiMap.create();
		map.putAll(ShardSetLoader.BY_ID);
		map.putAll(REGISTERED_SHARDS);
		return map;
	}

	private static BiMap<Identifier, ShardType> createShardTypes() {
		BiMap<Identifier, ShardType> map = HashBiMap.create();
		map.putAll(ShardTypeLoader.MAP);
		map.putAll(REGISTERED_SHARD_TYPES);
		return map;
	}

	public static void updateShardTypes() {
		shardTypes = createShardTypes();
	}

	public static void updateShards() {
		shardSets = createShardSets();
		shardData = createShardData();
	}
}
