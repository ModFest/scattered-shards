package net.modfest.scatteredshards.api;

import java.util.stream.Stream;

import com.google.common.collect.SetMultimap;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

/**
 * Represents a shard library, the set of all shards which exist in a particular context.
 */
public interface ShardLibrary {
	
	public MiniRegistry<Shard> shards();
	public MiniRegistry<ShardType> shardTypes();
	public SetMultimap<Identifier, Identifier> shardSets();
	
	/**
	 * Removes all Shards, ShardTypes, and ShardSets in this Library.
	 */
	public void clearAll();
	
	public Stream<Shard> resolveShardSet(Identifier id);
}
