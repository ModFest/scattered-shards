package net.modfest.scatteredshards.api.impl;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.api.MiniRegistry;
import net.modfest.scatteredshards.api.ShardDisplaySettings;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

import java.util.Optional;
import java.util.stream.Stream;

public class ShardLibraryImpl implements ShardLibrary {
	private final MiniRegistry<Shard> shards;
	private final MiniRegistry<ShardType> shardTypes;
	private final SetMultimap<ResourceLocation, ResourceLocation> shardSets;
	private final ShardDisplaySettings shardDisplaySettings;

	@Override
	public void clearAll() {
		shards.clear();
		shardSets.clear();
		shardTypes.clear();
	}

	public ShardLibraryImpl() {
		this(
			new MiniRegistry<>(Shard.CODEC),
			new MiniRegistry<>(ShardType.CODEC),
			MultimapBuilder.hashKeys().hashSetValues(3).build(),
			new ShardDisplaySettings()
		);
	}

	public ShardLibraryImpl(MiniRegistry<Shard> shards, MiniRegistry<ShardType> shardTypes, SetMultimap<ResourceLocation, ResourceLocation> shardSets, ShardDisplaySettings settings) {
		this.shards = shards;
		this.shardTypes = shardTypes;
		this.shardSets = shardSets;
		this.shardDisplaySettings = settings;
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
	public SetMultimap<ResourceLocation, ResourceLocation> shardSets() {
		return shardSets;
	}

	@Override
	public ShardDisplaySettings shardDisplaySettings() {
		return shardDisplaySettings;
	}

	@Override
	public Stream<Shard> resolveShardSet(ResourceLocation id) {
		return shardSets.get(id).stream()
			.map(shards::get)
			.flatMap(Optional::stream);
	}
}
