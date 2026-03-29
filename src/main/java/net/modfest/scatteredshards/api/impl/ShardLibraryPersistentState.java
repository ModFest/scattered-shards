package net.modfest.scatteredshards.api.impl;

import com.mojang.serialization.Codec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardLibraryPersistentState extends SavedData {
	public static final Codec<ShardLibraryPersistentState> CODEC = CompoundTag.CODEC.xmap(
		ShardLibraryPersistentState::createFromNbt,
		ShardLibraryPersistentState::writeNbt
	);

	private static final SavedDataType<ShardLibraryPersistentState> TYPE = new SavedDataType<>(ScatteredShards.id("library"),
		ShardLibraryPersistentState::new,
		ShardLibraryPersistentState.CODEC,
		null);

	public static final String SHARDS_KEY = "Shards";

	public static ShardLibraryPersistentState get(MinecraftServer server) {
		return server.overworld().getDataStorage().computeIfAbsent(TYPE);
	}

	public ShardLibraryPersistentState() {
	}

	public static ShardLibraryPersistentState createFromNbt(CompoundTag tag) {
		ScatteredShards.LOGGER.info("Loading shard library...");
		ShardLibraryPersistentState state = new ShardLibraryPersistentState();
		// This is just a placeholder - all the data lives in the serverLibrary below

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		library.shards().clear();
		library.shardSets().clear();

		CompoundTag shards = tag.getCompound(SHARDS_KEY).get();
		for (String id : shards.keySet()) {
			try {
				CompoundTag shardNbt = shards.getCompound(id).get();
				Identifier shardId = Identifier.parse(id);
				Shard shard = Shard.fromNbt(shardNbt);

				library.shards().put(shardId, shard);
				library.shardSets().put(shard.sourceId(), shardId);
			} catch (Throwable t) {
				ScatteredShards.LOGGER.error("Could not load shard \"{}\": {}", id, t.getMessage());
			}
		}

		ScatteredShards.LOGGER.info("Loaded {} shards and {} shardSets.", library.shards().size(), library.shardSets().size());

		return state;
	}

	public CompoundTag writeNbt() {
		CompoundTag tag = new CompoundTag();

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		ScatteredShards.LOGGER.info("Saving the ShardLibrary with {} shards and {} shardSets...", library.shards().size(), library.shardSets().size());

		tag.put(SHARDS_KEY, library.shards().toNbt());

		ScatteredShards.LOGGER.info("ShardLibrary saved.");

		return tag;
	}
}
