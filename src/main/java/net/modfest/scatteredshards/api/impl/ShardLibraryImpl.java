package net.modfest.scatteredshards.api.impl;

import java.util.Optional;
import java.util.stream.Stream;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.MiniRegistry;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class ShardLibraryImpl implements ShardLibrary {
	private MiniRegistry<Shard> shards = new MiniRegistry<>(Shard.CODEC);
	private MiniRegistry<ShardType> shardTypes = new MiniRegistry<ShardType>(ShardType.CODEC);
	private SetMultimap<Identifier, Identifier> shardSets = MultimapBuilder.hashKeys().hashSetValues(3).build();
	
	@Override
	public void clearAll() {
		shards.clear();
		shardSets.clear();
		shardTypes.clear();
	}
	
	@Override
	public MiniRegistry<Shard> shards() {
		return shards;
	}
	
	@Override
	public MiniRegistry<ShardType> shardTypes() {
		return shardTypes;
	}
	
	@Override
	public SetMultimap<Identifier, Identifier> shardSets() {
		return shardSets;
	}
	
	@Override
	public Stream<Shard> resolveShardSet(Identifier id) {
		return shardSets.get(id).stream()
			.map(shards::get)
			.flatMap(Optional::stream);
	}
}
