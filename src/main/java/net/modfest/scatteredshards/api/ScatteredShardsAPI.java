package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;

public class ScatteredShardsAPI {

	public static Multimap<Identifier, Shard> getShardSets() {
		return ScatteredShardsAPIImpl.shardSets;
	}

	public static BiMap<Identifier, Shard> getShardData() {
		return ScatteredShardsAPIImpl.shardData;
	}
}
