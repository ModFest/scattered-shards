package net.modfest.scatteredshards.api;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;

/**
 * Little wrapper around BiMap to optionalize some things
 */
public class MiniRegistry<T> {
	private BiMap<Identifier, T> data = HashBiMap.create();
	private UnboundedMapCodec<Identifier, T> mapCodec;
	
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
	
	public void remove(Identifier id) {
		data.remove(id);
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
}
