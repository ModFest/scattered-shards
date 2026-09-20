package net.modfest.scatteredshards.api.impl;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.MiniRegistry;
import net.modfest.scatteredshards.api.ShardDisplaySettings;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("ClassCanBeRecord")
public class ShardLibraryImpl implements ShardLibrary {
	private final MiniRegistry<Shard> shards;
	private final MiniRegistry<ShardType> shardTypes;
	private final SetMultimap<Identifier, Identifier> shardSets;
	private final ShardDisplaySettings shardDisplaySettings;

	@Override
	public void clearAll() {
		this.shards.clear();
		this.shardSets.clear();
		this.shardTypes.clear();
	}

	public ShardLibraryImpl() {
		this(
			new MiniRegistry<>(Shard.CODEC),
			new MiniRegistry<>(ShardType.CODEC),
			MultimapBuilder.hashKeys().hashSetValues(3).build(),
			new ShardDisplaySettings()
		);
	}

	public ShardLibraryImpl(MiniRegistry<Shard> shards, MiniRegistry<ShardType> shardTypes, SetMultimap<Identifier, Identifier> shardSets, ShardDisplaySettings settings) {
		this.shards = shards;
		this.shardTypes = shardTypes;
		this.shardSets = shardSets;
		this.shardDisplaySettings = settings;
	}

	@Override
	public MiniRegistry<Shard> shards() {
		return this.shards;
	}

	@Override
	public MiniRegistry<ShardType> shardTypes() {
		return this.shardTypes;
	}

	@Override
	public SetMultimap<Identifier, Identifier> shardSets() {
		return this.shardSets;
	}

	@Override
	public ShardDisplaySettings shardDisplaySettings() {
		return this.shardDisplaySettings;
	}

	@Override
	public Stream<Shard> resolveShardSet(Identifier id) {
		return this.shardSets.get(id).stream()
			.map(this.shards::get)
			.flatMap(Optional::stream);
	}
}
