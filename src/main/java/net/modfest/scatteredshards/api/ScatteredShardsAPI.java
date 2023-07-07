package net.modfest.scatteredshards.api;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.ShardData;
import net.modfest.scatteredshards.core.api.shard.ShardSet;

import java.util.Map;

public class ScatteredShardsAPI {

	public static Map<Identifier, ShardSet> getShardSets() {
		return ScatteredShardsAPIImpl.getOrCreateShardSets();
	}

	public static Map<Identifier, ShardData> getShardData() {
		return ScatteredShardsAPIImpl.getOrCreateShardData();
	}
}
