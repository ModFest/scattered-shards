package net.modfest.scatteredshards.api.impl;

import java.util.Map;
import java.util.UUID;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;

public class ShardCollectionPersistentState extends PersistentState {
	
	public static PersistentState.Type<ShardCollectionPersistentState> TYPE = new PersistentState.Type<>(
			ShardCollectionPersistentState::new,
			ShardCollectionPersistentState::createFromNbt,
			null
			);
	
	public static ShardCollectionPersistentState get(MinecraftServer server) {
		var result = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ScatteredShards.ID+"_collections");
		ScatteredShardsAPI.register(result);
		return result;
	}
	
	public static ShardCollectionPersistentState createFromNbt(NbtCompound tag) {
		ShardCollectionPersistentState state = new ShardCollectionPersistentState();
		
		for(String s : tag.getKeys()) {
			try {
				UUID uuid = UUID.fromString(s);
				ShardCollection coll = ScatteredShardsAPI.getServerCollection(uuid);
				coll.clear();
				
				for(NbtElement elem : tag.getList(s, NbtElement.STRING_TYPE)) {
					if (elem instanceof NbtString str) {
						Identifier shardId = new Identifier(str.asString());
						coll.add(shardId);
					}
				};
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load collection for uuid \"" + s + "\": " + t.getLocalizedMessage());
			}
		}
		/* TODO: Load data in. Later we can go user by user if things get mega laggy, but in the grand scheme of things,
		 * even for a thousand or two users, it's not that much data compared to one chest full of forestry saplings. */
		
		return state;
	}
	
	@Override
	public NbtCompound writeNbt(NbtCompound tag) {
		ScatteredShards.LOGGER.info("Saving shard collections...");
		
		Map<UUID, ShardCollection> collections = ScatteredShardsAPI.exportServerCollections();
		collections.forEach((id, collection) -> {
			NbtList list = new NbtList();
			for(Identifier i : collection) {
				list.add(NbtString.of(i.toString()));
			}
			tag.put(id.toString(), list);
		});
		
		return tag;
	}

}
