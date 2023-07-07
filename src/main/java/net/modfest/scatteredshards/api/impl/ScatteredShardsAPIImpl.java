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
	public static Multimap<Identifier, Shard> shardSets = null;
	public static BiMap<Identifier, Shard> shardData = null;

	static {
		update();
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

	public static void update() {
		shardSets = createShardSets();
		shardData = createShardData();
	}
}
