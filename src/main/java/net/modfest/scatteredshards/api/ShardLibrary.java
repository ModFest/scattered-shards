package net.modfest.scatteredshards.api;

import java.util.Collection;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;

public interface ShardLibrary {
	public boolean contains(Identifier id);
	public int size();
	public void clear();
	public Shard getShard(Identifier id);
	public @Nullable Identifier getId(Shard shard);
	public void putShard(Identifier shardId, Shard newData);
	public void deleteShard(Identifier shardId);
	public Collection<Identifier> getShardIds();
	public Collection<Identifier> getShardSets();
	public Collection<Identifier> getShardSet(Identifier id);
	public Stream<Shard> getResolvedShardSet(Identifier id);
}
