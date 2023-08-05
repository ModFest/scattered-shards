package net.modfest.scatteredshards.component;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.level.LevelComponents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;

/**
 * Represents the entire library of shards on the server or singleplayer game.
 */
public class ShardLibraryComponent implements Component, AutoSyncedComponent {
	public static final String LIBRARY_KEY = "Library";
	
	@SuppressWarnings("unused")
	private WorldProperties provider;
	private BiMap<Identifier, Shard> data = HashBiMap.create();
	
	public ShardLibraryComponent(WorldProperties provider) {
		this.provider = provider;
	}
	
	public Shard getShard(Identifier id) {
		Shard result = data.get(id);
		if (result != null) return result;
		result = ScatteredShardsAPI.getShardData().get(id);
		return (result != null) ? result : Shard.MISSING_SHARD;
	}
	
	public @Nullable Identifier getId(Shard shard) {
		Identifier result = data.inverse().get(shard);
		if (result != null) return result;
		result = ScatteredShardsAPI.getShardData().inverse().get(shard);
		return result;
	}
	
	/**
	 * Probably don't alter cards on the client!
	 */
	public void modifyShard(Identifier shardId, Shard newData, World world) {
		data.put(shardId, newData);
		MinecraftServer server = world.getServer();
		if (server == null) return;
		LevelComponents.sync(ScatteredShardsComponents.LIBRARY, server); //TODO: Send a smaller packet to all players currently connected containing the shard added/modified
	}
	
	@Override
	public void readFromNbt(NbtCompound tag) {
		NbtCompound library = tag.getCompound(LIBRARY_KEY);
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
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtCompound library = new NbtCompound();
		tag.put(LIBRARY_KEY, library);
		for(Map.Entry<Identifier, Shard> entry : data.entrySet()) {
			library.put(entry.getKey().toString(), entry.getValue().writeNbt(new NbtCompound()));
		}
	}

}
