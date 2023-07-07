package net.modfest.scatteredshards.api;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

public class ScatteredShardsAPI {

	public static Multimap<Identifier, Shard> getShardSets() {
		return ScatteredShardsAPIImpl.getOrCreateShardSets();
	}

	public static BiMap<Identifier, Shard> getShardData() {
		return ScatteredShardsAPIImpl.getOrCreateShardData();
	}
}
