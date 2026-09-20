package net.modfest.scatteredshards.api.impl;

import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.ShardCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@SuppressWarnings("ClassCanBeRecord")
public class ShardCollectionImpl implements ShardCollection {
	private final Set<Identifier> data;

	public ShardCollectionImpl() {
		this(new HashSet<>());
	}

	public ShardCollectionImpl(Set<Identifier> data) {
		this.data = data;
	}

	@Override
	public boolean contains(Identifier shardId) {
		return this.data.contains(shardId);
	}

	@Override
	public boolean add(Identifier shardId) {
		return this.data.add(shardId);
	}

	@Override
	public void addAll(Collection<Identifier> shardIds) {
		this.data.addAll(shardIds);
	}

	@Override
	public boolean remove(Identifier shardId) {
		return this.data.remove(shardId);
	}

	@Override
	public int size() {
		return this.data.size();
	}

	@Override
	public void clear() {
		this.data.clear();
	}

	@Override
	public @NotNull Iterator<Identifier> iterator() {
		return this.data.iterator();
	}

	@Override
	public Set<Identifier> toImmutableSet() {
		return Set.copyOf(this.data);
	}
}
