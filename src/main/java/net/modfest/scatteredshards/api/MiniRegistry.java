package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

/**
 * Little wrapper around BiMap to optionalize some things
 */
public class MiniRegistry<T> {
	private final BiMap<ResourceLocation, T> data = HashBiMap.create();
	private final UnboundedMapCodec<ResourceLocation, T> mapCodec;

	public MiniRegistry(Codec<T> valueCodec) {
		this.mapCodec = Codec.unboundedMap(ResourceLocation.CODEC, valueCodec);
	}

	public Optional<T> get(ResourceLocation id) {
		return Optional.ofNullable(data.get(id));
	}

	public Optional<ResourceLocation> get(T value) {
		return Optional.ofNullable(data.inverse().get(value));
	}

	public void forEach(BiConsumer<ResourceLocation, T> consumer) {
		data.forEach(consumer);
	}

	public Stream<ResourceLocation> streamKeys() {
		return data.keySet().stream();
	}

	public void put(ResourceLocation id, T value) {
		data.put(id, value);
	}

	public void putAll(Map<ResourceLocation, T> values) {
		data.putAll(values);
	}

	public void remove(ResourceLocation id) {
		data.remove(id);
	}

	public void removeAll(Collection<ResourceLocation> ids) {
		data.keySet().removeAll(ids);
	}

	public void clear() {
		data.clear();
	}

	public int size() {
		return data.size();
	}

	public CompoundTag toNbt() {
		return (CompoundTag) mapCodec.encodeStart(NbtOps.INSTANCE, data).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) mapCodec.encodeStart(JsonOps.INSTANCE, data).result().orElseThrow();
	}

	public <U> void syncFrom(DynamicOps<U> sourceDataFlavor, U sourceData) {
		mapCodec.parse(sourceDataFlavor, sourceData).result().ifPresent(it -> {
			data.clear();
			data.putAll(it);
		});
	}

	public void syncFromNbt(CompoundTag tag) {
		syncFrom(NbtOps.INSTANCE, tag);
	}

	public void syncFromJson(JsonObject obj) {
		syncFrom(JsonOps.INSTANCE, obj);
	}

	// this is not great
	public static <T> StreamCodec<RegistryFriendlyByteBuf, MiniRegistry<T>> createPacketCodec(Codec<T> valueCodec) {
		return ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.fromCodec(valueCodec)).map(
			map -> {
				MiniRegistry<T> registry = new MiniRegistry<>(valueCodec);
				registry.putAll(map);
				return registry;
			},
			registry -> new HashMap<>(registry.data)
		).cast();
	}
}
