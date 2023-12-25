package net.modfest.scatteredshards.api;

import java.util.Optional;
import java.util.function.BiConsumer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.UnboundedMapCodec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Identifier;

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
		return (NbtCompound) mapCodec.encodeStart(NbtOps.INSTANCE, data).result().get();
		/*
		Codec.unboundedMap(Identifier.CODEC, valueCodec);
		forEach((id, value) -> {
			valueCodec.encodeStart(NbtOps.INSTANCE, value).result().ifPresent((it) -> {
				tag.put(id.toString(), it);
			});
		});
		return tag;*/
	}
	
	public void syncFromNbt(NbtCompound tag) {
		mapCodec.parse(NbtOps.INSTANCE, tag).result().ifPresent(it -> {
			data.clear();
			data.putAll(it);
		});
		
		/*
		data.clear();
		for(String key : tag.getKeys()) {
			valueCodec.parse(NbtOps.INSTANCE, tag.get(key))
				.result().ifPresent((it) -> {
					this.put(new Identifier(key), it);
				});
		}*/
	}
}
