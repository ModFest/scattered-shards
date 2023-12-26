package net.modfest.scatteredshards.api.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class ShardLibraryPersistentState extends PersistentState {
	public static PersistentState.Type<ShardLibraryPersistentState> TYPE = new PersistentState.Type<>(
			ShardLibraryPersistentState::new,
			ShardLibraryPersistentState::createFromNbt,
			null
			);
	
	public static final String SHARD_TYPES_KEY = "ShardTypes";
	public static final String SHARDS_KEY = "Shards";
	public static final String SHARD_SETS_KEY = "ShardSets";
	
	public static ShardLibraryPersistentState get(MinecraftServer server) {
		ShardLibraryPersistentState result = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ScatteredShards.ID);
		System.out.println("PersistentState acquired: "+ScatteredShardsAPI.getServerLibrary().shards().size()+" shards.");
		result.markDirty();
		return result;
	}
	
	public static ShardLibraryPersistentState createFromNbt(NbtCompound tag) {
		ScatteredShards.LOGGER.info("Loading shard library...");
		ShardLibraryPersistentState state = new ShardLibraryPersistentState();
		// This is just a placeholder - all the data lives in the serverLibrary below
		
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.clearAll();
		
		NbtCompound shardTypes = tag.getCompound(SHARD_TYPES_KEY);
		if (shardTypes.isEmpty() || (shardTypes.getSize() == 1 && shardTypes.contains(ShardType.MISSING_ID.toString()))) {
			//Either the ShardTypes were completely empty, or the only ShardType present is the missing type.
			
			//TODO: Load shardTypes from resources
			//For now, we're preloading with the default types if none are present.
			library.shardTypes().put(ScatteredShards.id("visitor"), ShardType.VISITOR);
			library.shardTypes().put(ScatteredShards.id("challenge"), ShardType.CHALLENGE);
			library.shardTypes().put(ScatteredShards.id("secret"), ShardType.SECRET);
			library.shardTypes().put(ShardType.MISSING_ID, ShardType.MISSING);
		} else {
			for(String id : shardTypes.getKeys()) {
				try {
					NbtCompound shardNbt = shardTypes.getCompound(id);
					library.shardTypes().put(new Identifier(id), ShardType.fromNbt(shardNbt));
					
				} catch (Throwable t) {
					ScatteredShards.LOGGER.error("Could not load shardType \""+id+"\": " + t.getMessage());
				}
			}
		}
		
		NbtCompound shards = tag.getCompound(SHARDS_KEY);
		for(String id : shards.getKeys()) {
			try {
				NbtCompound shardNbt = shards.getCompound(id);
				library.shards().put(new Identifier(id), Shard.fromNbt(shardNbt));
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load shard \""+id+"\": " + t.getMessage());
			}
		}
		
		NbtCompound shardSets = tag.getCompound(SHARD_SETS_KEY);
		for(String id : shardSets.getKeys()) {
			try {
				
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load shardSet \""+id+"\": " + t.getMessage());
			}
		}
		
		if (library.shardTypes().size() == 1 && library.shardTypes().streamKeys().findFirst().get().equals(ShardType.MISSING_ID)) {
			
		}
		
		ScatteredShards.LOGGER.info("Finished loading " + library.shardTypes().size() + " shard types, " + library.shards().size() + " shards, and " + library.shardSets().size() + " shardSets.");
		
		return state;
	}
	
	
	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		System.out.println("Saving " + library.shards().size() + " shards to NBT!");
		
		tag.put(SHARD_TYPES_KEY, library.shardTypes().toNbt());
		tag.put(SHARDS_KEY, library.shards().toNbt());
		
		NbtCompound shardSets = new NbtCompound();
		library.shardSets().asMap().forEach((id, set) -> {
			NbtList list = new NbtList();
			for(Identifier i : set) {
				list.add(NbtString.of(i.toString()));
			}
			
		});
		tag.put(SHARD_SETS_KEY, shardSets);
		
		return tag;
	}
	
}