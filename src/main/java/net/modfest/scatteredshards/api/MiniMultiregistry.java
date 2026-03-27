package net.modfest.scatteredshards.api;

import com.google.common.base.Functions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Little wrap around Multimap to make it easier to manage as a registry
 */
public class MiniMultiregistry<T> {
	private final Multimap<Identifier, T> data = MultimapBuilder.hashKeys().hashSetValues(3).build();
	private final Codec<Map<Identifier, Collection<T>>> mapCodec;

	public MiniMultiregistry(Codec<T> valueCodec) {
		mapCodec = Codec.unboundedMap(Identifier.CODEC, valueCodec.listOf().xmap(Functions.identity(), List::copyOf));
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

	public CompoundTag toNbt() {
		return (CompoundTag) mapCodec.encodeStart(NbtOps.INSTANCE, data.asMap()).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) mapCodec.encodeStart(JsonOps.INSTANCE, data.asMap()).result().orElseThrow();
	}

	public <U> void syncFrom(DynamicOps<U> sourceDataType, U sourceData) {
		mapCodec.parse(sourceDataType, sourceData).result().ifPresent(it -> {
			data.clear();

			it.forEach(data::putAll);
		});
	}

	public void syncFromNbt(CompoundTag tag) {
		syncFrom(NbtOps.INSTANCE, tag);
	}

	public void syncFromJson(JsonObject obj) {
		syncFrom(JsonOps.INSTANCE, obj);
	}
}
