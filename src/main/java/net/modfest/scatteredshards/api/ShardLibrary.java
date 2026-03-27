package net.modfest.scatteredshards.api;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Represents a shard library, the set of all shards which exist in a particular context.
 */
public interface ShardLibrary {

	MiniRegistry<Shard> shards();

	MiniRegistry<ShardType> shardTypes();

	SetMultimap<Identifier, Identifier> shardSets();

	ShardDisplaySettings shardDisplaySettings();

	/**
	 * Removes all Shards, ShardTypes, and ShardSets in this Library.
	 */
	void clearAll();

	Stream<Shard> resolveShardSet(Identifier id);

	// this is just the worst
	StreamCodec<RegistryFriendlyByteBuf, ShardLibrary> PACKET_CODEC = StreamCodec.composite(
		MiniRegistry.createPacketCodec(Shard.CODEC), ShardLibrary::shards,
		MiniRegistry.createPacketCodec(ShardType.CODEC), ShardLibrary::shardTypes,
		ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC)).map(
			map -> {
				SetMultimap<Identifier, Identifier> multimap = MultimapBuilder.hashKeys().hashSetValues(3).build();
				for (Map.Entry<Identifier, ArrayList<Identifier>> entry : map.entrySet()) {
					multimap.putAll(entry.getKey(), entry.getValue());
				}
				return multimap;
			},
			multimap -> {
				HashMap<Identifier, ArrayList<Identifier>> map = new HashMap<>();
				for (Map.Entry<Identifier, Collection<Identifier>> entry : multimap.asMap().entrySet()) {
					map.put(entry.getKey(), new ArrayList<>(entry.getValue()));
				}
				return map;
			}
		).cast(),
		ShardLibrary::shardSets,
		ShardDisplaySettings.PACKET_CODEC, ShardLibrary::shardDisplaySettings,
		ShardLibraryImpl::new
	);
}
