package net.modfest.scatteredshards.api.impl;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;

import java.util.Map;
import java.util.UUID;

public class ShardCollectionPersistentState extends PersistentState {

	public static PersistentState.Type<ShardCollectionPersistentState> TYPE = new PersistentState.Type<>(
		ShardCollectionPersistentState::new,
		ShardCollectionPersistentState::createFromNbt,
		null
	);

	public static ShardCollectionPersistentState get(MinecraftServer server) {
		ShardCollectionPersistentState result = server.getOverworld().getPersistentStateManager().getOrCreate(TYPE, ScatteredShards.ID + "_collections");
		ScatteredShardsAPI.register(result);
		return result;
	}

	public static ShardCollectionPersistentState createFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
		ShardCollectionPersistentState state = new ShardCollectionPersistentState();
		ScatteredShards.LOGGER.info("Loading shard collections for {} players...", tag.getSize());

		for (String s : tag.getKeys()) {
			try {
				UUID uuid = UUID.fromString(s);
				ShardCollection coll = ScatteredShardsAPI.getServerCollection(uuid);
				coll.clear();

				for (NbtElement elem : tag.getList(s, NbtElement.STRING_TYPE)) {
					if (elem instanceof NbtString str) {
						Identifier shardId = Identifier.of(str.asString());
						coll.add(shardId);
					}
				}
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load collection for uuid \"{}\": {}", s, t.getLocalizedMessage());
			}
		}
		/* Later we can go user by user if things get mega laggy. But in the grand scheme of things, even for a thousand
		 * or two users, it's not that much data compared to one chest full of forestry saplings. */

		ScatteredShards.LOGGER.info("Collections loaded.");

		return state;
	}

	@Override
	public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registryLookup) {
		Map<UUID, ShardCollection> collections = ScatteredShardsAPI.exportServerCollections();
		ScatteredShards.LOGGER.info("Saving ShardCollections for {} players...", collections.size());

		collections.forEach((id, collection) -> {
			NbtList list = new NbtList();
			for (Identifier i : collection) {
				list.add(NbtString.of(i.toString()));
			}
			tag.put(id.toString(), list);
		});

		ScatteredShards.LOGGER.info("ShardCollections saved.");
		return tag;
	}
}
