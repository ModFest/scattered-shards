package net.modfest.scatteredshards.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.impl.ShardCollectionImpl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public interface ShardCollection extends Iterable<Identifier> {
	boolean contains(Identifier identifier);

	/**
	 * Adds the identified Shard to this Collection.
	 *
	 * @param shardId The Id of the Shard to add. No check is made to see whether the Shard exists.
	 * @return true if the shard was added; false if that ShardId was already in the library or could not be added.
	 */
	boolean add(Identifier shardId);

	/**
	 * Removes the specified Shard from this Collection.
	 *
	 * @param shardId the Id of the Shard to remove.
	 */
	boolean remove(Identifier shardId);

	int size();

	void clear();

	Set<Identifier> toImmutableSet();

	void addAll(Collection<Identifier> shardIds);

	StreamCodec<RegistryFriendlyByteBuf, ShardCollection> PACKET_CODEC = ByteBufCodecs
		.collection(HashSet::new, Identifier.STREAM_CODEC)
		.map(set -> (ShardCollection) new ShardCollectionImpl(set), collection -> new HashSet<>(collection.toImmutableSet()))
		.cast();
}
