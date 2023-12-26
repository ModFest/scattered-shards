package net.modfest.scatteredshards.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.google.common.base.Functions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

/**
 * Little wrap around Multimap to make it easier to manage as a registry
 */
public class MiniMultiregistry<T> {
	private Multimap<Identifier, T> data = MultimapBuilder.hashKeys().hashSetValues(3).build();
	private Codec<Map<Identifier, Collection<T>>> mapCodec;
	
	public MiniMultiregistry(Codec<T> valueCodec) {
		mapCodec = Codec.unboundedMap(Identifier.CODEC, valueCodec.listOf().<Collection<T>>xmap(Functions.identity(), List::copyOf));
	}
	
	public Collection<T> get(Identifier id) {
		return data.get(id);
	}
	
	public void forEachMapping(BiConsumer<Identifier, T> consumer) {
		data.forEach(consumer);
	}
	
	public void forEachSet(BiConsumer<Identifier, Collection<T>> consumer) {
		data.asMap().forEach(consumer);
	}
	
	public void put(Identifier key, T value) {
		data.put(key, value);
	}
	
	public void removeValue(Identifier key, T value) {
		data.remove(key, value);
	}
	
	public void removeKey(Identifier key) {
		data.removeAll(key);
	}
	
	public void clear() {
		data.clear();
	}
	
	public NbtCompound toNbt() {
		return (NbtCompound) mapCodec.encodeStart(NbtOps.INSTANCE, data.asMap()).result().orElseThrow();
	}
	
	public JsonObject toJson() {
		return (JsonObject) mapCodec.encodeStart(JsonOps.INSTANCE, data.asMap()).result().orElseThrow();
	}
	
	public <U> void syncFrom(DynamicOps<U> sourceDataType, U sourceData) {
		mapCodec.parse(sourceDataType, sourceData).result().ifPresent(it -> {
			data.clear();
			
			it.forEach((id, list) -> {
				data.putAll(id, list);
			});
		});
	}
	
	public void syncFromNbt(NbtCompound tag) {
		syncFrom(NbtOps.INSTANCE, tag);
	}
	
	public void syncFromJson(JsonObject obj) {
		syncFrom(JsonOps.INSTANCE, obj);
	}
}
