package net.modfest.scatteredshards.api.impl;

import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.api.ShardCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ShardCollectionImpl implements ShardCollection {
	private final Set<ResourceLocation> data;

	public ShardCollectionImpl() {
		this(new HashSet<>());
	}

	public ShardCollectionImpl(Set<ResourceLocation> data) {
		this.data = data;
	}

	@Override
	public boolean contains(ResourceLocation shardId) {
		return data.contains(shardId);
	}

	@Override
	public boolean add(ResourceLocation shardId) {
		return data.add(shardId);
	}

	@Override
	public void addAll(Collection<ResourceLocation> shardIds) {
		data.addAll(shardIds);
	}

	@Override
	public boolean remove(ResourceLocation shardId) {
		return data.remove(shardId);
	}

	@Override
	public int size() {
		return data.size();
	}

	@Override
	public void clear() {
		data.clear();
	}

	@Override
	public @NotNull Iterator<ResourceLocation> iterator() {
		return data.iterator();
	}

	@Override
	public Set<ResourceLocation> toImmutableSet() {
		return Set.copyOf(data);
	}
}
