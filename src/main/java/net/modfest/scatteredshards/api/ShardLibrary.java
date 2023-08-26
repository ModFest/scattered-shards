package net.modfest.scatteredshards.api;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Common functions for all shard library implementations.
 * 
 * <p>A Shard Library is conceptually a reversible mapping from shard Identifiers to Shard objects, with a secondary
 * mapping of shardSetId's to shardIds.
 * 
 * <p>Note: ShardSets and ShardSources are usually the same thing. The terms are often used interchangeably throughout
 * the codebase, but if the two should come to mean different things, here we say and mean ShardSet.
 * 
 */
public interface ShardLibrary {
	/**
	 * Gets whether this Library contains a shard with the specified id
	 * @param id The id to search for in this Library
	 * @return true if this Library contains the shard, otherwise false
	 */
	@Contract(pure = true)
	public boolean contains(Identifier id);
	
	/**
	 * Gets the number of shards in this Library
	 * @return the number of shards in this Library
	 */
	@Contract(pure = true)
	public int size();
	
	/**
	 * Gets the Shard object referred to by the specified id
	 * @param id the id to search for in this Library
	 * @return the shard for the specified id if present, or the Missing Shard if not present
	 */
	@Contract(pure = true)
	public @NotNull Shard getShard(Identifier id);
	
	/**
	 * Gets the id for the specified Shard
	 * @param shard the shard to search for in this Library
	 * @return the id for the Shard, or null of the Shard is not in this Library
	 */
	@Contract(pure = true)
	public @Nullable Identifier getId(Shard shard);
	
	/**
	 * Gets an immutable view of the ids of the shards in this Library
	 * @return a Collection of shard ids identifying the shards in this Library. If this Library is emtpy, an empty
	 *         Collection will be returned
	 */
	@Contract(pure = true)
	public @NotNull Collection<Identifier> getShardIds();
	
	/**
	 * Gets an immutable view of the ids of the shardSets in this Library
	 * @return a Collection of shardSet ids identifying the shardSets in this Library. If this Library is empty, an
	 *         empty Collection will be returned
	 */
	@Contract(pure = true)
	public @NotNull Collection<Identifier> getShardSets();
	
	/**
	 * Gets an immutable view of the shardIds in the specified shardSet
	 * @param id the shardSet id to search for
	 * @return a Collection of shard ids indentifying the shards in the shardSet. If the shardSet is empty or unknown,
	 *         an empty Collection will be returned.
	 */
	@Contract(pure = true)
	public Collection<Identifier> getShardSet(Identifier id);
	
	/**
	 * Gets a Stream of the Shards in the specified shardSet.
	 * @param id the shardSet id to search for
	 * @return a Stream of the Shard objects, if any, contained in the shardSet. If the shardSet is empty or unknown,
	 *         an empty Stream will be returned.
	 */
	@Contract(pure = true)
	public Stream<Shard> getResolvedShardSet(Identifier id);
	
	/**
	 * Clears all shards from this Library
	 * 
	 * <p>Note; Since a shard library is often a collaborative work representing a lot of
	 * effort from a lot of people, this is a very dangerous operation, and should be strictly access-controlled
	 */
	public void clear();
	
	/**
	 * Adds or replaces a shard in this Library. This will also affect the mapping of shardSets to shardIds
	 * @param shardId the id to add or modify
	 * @param newData the Shard object to map this id to
	 * @see java.util.Map#put(Object, Object)
	 */
	public void putShard(Identifier shardId, Shard newData);
	
	/**
	 * Removes a shard from this Library. This will also affect the mapping of shardSets to shardIds
	 * @param shardId the id of the Shard to remove from this Library.
	 */
	public void deleteShard(Identifier shardId);
}
