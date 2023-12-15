package net.modfest.scatteredshards.component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Stream;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.level.LevelComponents;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
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
	protected final Multimap<Identifier, Identifier> bySource = MultimapBuilder.linkedHashKeys().arrayListValues(3).build();
	
	public ShardLibraryComponent(WorldProperties provider) {
		this.provider = provider;
	}
	
	public boolean contains(Identifier id) {
		return data.containsKey(id) || ScatteredShardsAPI.getShardData().containsKey(id);
		
	}
	
	/**
	 * Returns the number of **mutable** shards only, not including datapack shards.
	 */
	public int size() {
		return data.size();
	}
	
	public void clear(World world) {
		data.clear();
		LevelComponents.sync(ScatteredShardsComponents.LIBRARY, world.getServer());
	}
	
	/**
	 * Searches for a shard in both the component and in data. Mutable component shards will "shadow" immutable data
	 * shards with the same shard Id, allowing even data-provided shards to be edited with modifyShard. If no shard of
	 * any type exists with this Id, {@link Shard#MISSING_SHARD MISSING_SHARD} is provided.
	 */
	public Shard getShard(Identifier id) {
		Shard result = data.get(id);
		if (result != null) return result;
		result = ScatteredShardsAPI.getShardData().get(id);
		return (result != null) ? result : Shard.MISSING_SHARD;
	}
	
	/**
	 * Gets the Id of the Shard provided. If the same shard has different Id's in the component and data, its component
	 * Id will be returned. Returns null if no shard exists with that Id.
	 */
	public @Nullable Identifier getId(Shard shard) {
		Identifier result = data.inverse().get(shard);
		if (result != null) return result;
		result = ScatteredShardsAPI.getShardData().inverse().get(shard);
		return result;
	}
	
	/**
	 * Adds, alters, or replaces a shard on the server, resyncing the shard with all connected players. Accessing this
	 * from the client may cause a desync.
	 */
	public void modifyShard(Identifier shardId, Shard newData, World world, PlayerEntity player) {
		data.put(shardId, newData);
		bySource.put(newData.sourceId(), shardId);
		MinecraftServer server = world.getServer();
		if (server == null) return;
		LevelComponents.sync(ScatteredShardsComponents.LIBRARY, server); //TODO: Send a smaller packet to all players currently connected containing the shard added/modified
		ScatteredShards.LOGGER.info("Shard '{}' was modified by player {} ({})", shardId, player.getUuid(), player.getDisplayName());
	}
	
	public void deleteShard(Identifier shardId, ServerWorld world, ServerCommandSource source) {
		MinecraftServer server = world.getServer();
		if (server == null) return;
		
		Shard shard = data.get(shardId);
		if (shard == null) return;
		
		data.remove(shardId);
		bySource.remove(shard.sourceId(), shardId);
		
		LevelComponents.sync(ScatteredShardsComponents.LIBRARY, server); //TODO: Send a smaller packet to all players currently connected to notify about shard deletion
		PlayerEntity player = source.getPlayer();
		if (player != null) {
			ScatteredShards.LOGGER.info("Shard '{}' was deleted by player {} ({})", shardId, player.getUuid(), player.getDisplayName());
		} else {
			ScatteredShards.LOGGER.info("Shard '{}' was deleted.", shardId);
		}
	}
	
	public Collection<Identifier> getShardIds() {
		HashSet<Identifier> all = new HashSet<>(data.keySet());
		all.addAll(ScatteredShardsAPI.getShardData().keySet());
		return all;
	}
	
	/**
	 * Gets the complete list of shard source Ids
	 * @return
	 */
	public Collection<Identifier> getShardSources() {
		return bySource.keySet();
	}
	
	/**
	 * Given a shardSet Id, gets a Collection of Shard Ids which are members of that shardSet. In other words, the
	 * returned shards will have the same sourceId as the passed-in Identifier.
	 * @param id An Identifier for a shardSet / shard sourceId
	 * @return A collection of ShardIds where the corresponding shards have the specified sourceId.
	 */
	public Collection<Identifier> getShardSet(Identifier id) {
		return bySource.get(id);
	}
	
	public Stream<Shard> getResolvedShardSet(Identifier id) {
		return bySource.get(id).stream().map(this::getShard);
	}
	
	@Override
	public void readFromNbt(NbtCompound tag) {
		data.clear();
		bySource.clear();
		NbtCompound library = tag.getCompound(LIBRARY_KEY);
		for(String k : library.getKeys()) {
			try {
				Identifier id = new Identifier(k);
				
				NbtCompound shardData = library.getCompound(k);
				Shard shard = Shard.fromNbt(shardData);
				
				data.put(id, shard);
				bySource.put(shard.sourceId(), id);
			} catch (InvalidIdentifierException | ArrayIndexOutOfBoundsException ex) {
				ScatteredShards.LOGGER.warn("Broken shard library data. Shard '"+k+"' was lost completely.", ex);
			}
		}
		
		// Include datapack shards in shardSet / shardSource mapping
		for(Map.Entry<Identifier, Shard> entry : ScatteredShardsAPI.getShardSets().entries()) {
			Identifier shardId = ScatteredShardsAPI.getShardData().inverse().get(entry.getValue());
			if (shardId != null) {
				bySource.put(entry.getKey(), shardId);
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
