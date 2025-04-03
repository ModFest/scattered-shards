package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Identifier;

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
	private final BiMap<Identifier, T> data = HashBiMap.create();
	private final UnboundedMapCodec<Identifier, T> mapCodec;

	public MiniRegistry(Codec<T> valueCodec) {
		this.mapCodec = Codec.unboundedMap(Identifier.CODEC, valueCodec);
	}

	public Optional<T> get(Identifier id) {
		return Optional.ofNullable(data.get(id));
	}

	public Optional<Identifier> get(T value) {
		return Optional.ofNullable(data.inverse().get(value));
	}

	public void forEach(BiConsumer<Identifier, T> consumer) {
		data.forEach(consumer);
	}

	public Stream<Identifier> streamKeys() {
		return data.keySet().stream();
	}

	public void put(Identifier id, T value) {
		data.put(id, value);
	}

	public void putAll(Map<Identifier, T> values) {
		data.putAll(values);
	}

	public void remove(Identifier id) {
		data.remove(id);
	}

	public void removeAll(Collection<Identifier> ids) {
		data.keySet().removeAll(ids);
	}

	public void clear() {
		data.clear();
	}

	public int size() {
		return data.size();
	}

	public NbtCompound toNbt() {
		return (NbtCompound) mapCodec.encodeStart(NbtOps.INSTANCE, data).result().orElseThrow();
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

	public void syncFromNbt(NbtCompound tag) {
		syncFrom(NbtOps.INSTANCE, tag);
	}

	public void syncFromJson(JsonObject obj) {
		syncFrom(JsonOps.INSTANCE, obj);
	}

	// this is not great
	public static <T> PacketCodec<RegistryByteBuf, MiniRegistry<T>> createPacketCodec(Codec<T> valueCodec) {
		return PacketCodecs.map(HashMap::new, Identifier.PACKET_CODEC, PacketCodecs.codec(valueCodec)).xmap(
			map -> {
				MiniRegistry<T> registry = new MiniRegistry<>(valueCodec);
				registry.putAll(map);
				return registry;
			},
			registry -> new HashMap<>(registry.data)
		).cast();
	}
}
