package net.modfest.scatteredshards.api.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ShardCollection;

public class ShardCollectionImpl implements ShardCollection {
	private Set<Identifier> data = new HashSet<>();
	
	@Override
	public boolean contains(Identifier shardId) {
		return data.contains(shardId);
	}
	
	@Override
	public boolean add(Identifier shardId) {
		return data.add(shardId);
	}
	
	@Override
	public void addAll(Collection<Identifier> shardIds) {
		data.addAll(shardIds);
	}

	@Override
	public boolean remove(Identifier shardId) {
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
	public Iterator<Identifier> iterator() {
		return data.iterator();
	}
	
	@Override
	public Set<Identifier> toImmutableSet() {
		return Set.copyOf(data);
	}
}
