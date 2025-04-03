package net.modfest.scatteredshards.api;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;
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
	PacketCodec<RegistryByteBuf, ShardLibrary> PACKET_CODEC = PacketCodec.tuple(
		MiniRegistry.createPacketCodec(Shard.CODEC), ShardLibrary::shards,
		MiniRegistry.createPacketCodec(ShardType.CODEC), ShardLibrary::shardTypes,
		PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.collection(ArrayList::new, Identifier.PACKET_CODEC)).xmap(
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
