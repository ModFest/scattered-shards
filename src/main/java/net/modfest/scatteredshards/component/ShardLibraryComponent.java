package net.modfest.scatteredshards.component;

import java.util.Map;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Represents the entire library of shards on the server or singleplayer game.
 */
public class ShardLibraryComponent implements Component {
	
	BiMap<Identifier, Shard> data = HashBiMap.create();
	
	@Override
	public void readFromNbt(NbtCompound tag) {
		NbtCompound library = tag.getCompound("Library");
		for(String k : library.getKeys()) {
			try {
				Identifier id = new Identifier(k);
				
				NbtCompound shardData = library.getCompound(k);
				Shard shard = Shard.fromNbt(shardData);
				
				data.put(id, shard);
			} catch (InvalidIdentifierException | ArrayIndexOutOfBoundsException ex) {
				ScatteredShards.LOGGER.warn("Broken shard library data. Shard '"+k+"' was lost completely.", ex);
			}
		}
		
		ScatteredShards.LOGGER.info("Recovered shards: "+data.keySet().toString());
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtCompound library = new NbtCompound();
		tag.put("Library", library);
		for(Map.Entry<Identifier, Shard> entry : data.entrySet()) {
			
		}
	}

}
