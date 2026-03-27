package net.modfest.scatteredshards.api;

import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.SetMultimap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
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

	SetMultimap<ResourceLocation, ResourceLocation> shardSets();

	ShardDisplaySettings shardDisplaySettings();

	/**
	 * Removes all Shards, ShardTypes, and ShardSets in this Library.
	 */
	void clearAll();

	Stream<Shard> resolveShardSet(ResourceLocation id);

	// this is just the worst
	StreamCodec<RegistryFriendlyByteBuf, ShardLibrary> PACKET_CODEC = StreamCodec.composite(
		MiniRegistry.createPacketCodec(Shard.CODEC), ShardLibrary::shards,
		MiniRegistry.createPacketCodec(ShardType.CODEC), ShardLibrary::shardTypes,
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.collection(ArrayList::new, ResourceLocation.STREAM_CODEC)).map(
			map -> {
				SetMultimap<ResourceLocation, ResourceLocation> multimap = MultimapBuilder.hashKeys().hashSetValues(3).build();
				for (Map.Entry<ResourceLocation, ArrayList<ResourceLocation>> entry : map.entrySet()) {
					multimap.putAll(entry.getKey(), entry.getValue());
				}
				return multimap;
			},
			multimap -> {
				HashMap<ResourceLocation, ArrayList<ResourceLocation>> map = new HashMap<>();
				for (Map.Entry<ResourceLocation, Collection<ResourceLocation>> entry : multimap.asMap().entrySet()) {
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
