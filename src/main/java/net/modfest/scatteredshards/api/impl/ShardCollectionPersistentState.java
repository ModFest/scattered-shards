package net.modfest.scatteredshards.api.impl;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;

import java.util.Map;
import java.util.UUID;

public class ShardCollectionPersistentState extends SavedData {
	public static final Codec<ShardCollectionPersistentState> CODEC = CompoundTag.CODEC.xmap(
		ShardCollectionPersistentState::createFromNbt,
		ShardCollectionPersistentState::writeNbt
	);

	private static final SavedDataType<ShardCollectionPersistentState> TYPE = new SavedDataType<>(ScatteredShards.ID + "_collections",
		ShardCollectionPersistentState::new,
		ShardCollectionPersistentState.CODEC,
		null);

	public static ShardCollectionPersistentState get(MinecraftServer server) {
		ShardCollectionPersistentState result = server.overworld().getDataStorage().computeIfAbsent(TYPE);
		ScatteredShardsAPI.register(result);
		return result;
	}

	public static ShardCollectionPersistentState createFromNbt(CompoundTag tag) {
		ShardCollectionPersistentState state = new ShardCollectionPersistentState();
		ScatteredShards.LOGGER.info("Loading shard collections for {} players...", tag.size());

		for (String s : tag.keySet()) {
			try {
				UUID uuid = UUID.fromString(s);
				ShardCollection coll = ScatteredShardsAPI.getServerCollection(uuid);
				coll.clear();

				for (Tag elem : tag.getList(s).get()) {
					if (elem instanceof StringTag str) {
						Identifier shardId = Identifier.parse(str.asString().get());
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

	public CompoundTag writeNbt() {
		CompoundTag tag = new CompoundTag();
		Map<UUID, ShardCollection> collections = ScatteredShardsAPI.exportServerCollections();
		ScatteredShards.LOGGER.info("Saving ShardCollections for {} players...", collections.size());

		collections.forEach((id, collection) -> {
			ListTag list = new ListTag();
			for (Identifier i : collection) {
				list.add(StringTag.valueOf(i.toString()));
			}
			tag.put(id.toString(), list);
		});

		ScatteredShards.LOGGER.info("ShardCollections saved.");
		return tag;
	}
}
