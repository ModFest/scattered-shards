package net.modfest.scatteredshards.api;

import com.google.common.collect.BiMap;
import com.google.common.collect.Multimap;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.api.impl.ShardLibraryImpl;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;

public class ScatteredShardsAPI {
	
	public static final String MODIFY_SHARD_PERMISSION = ScatteredShards.permission("modify_shard");
	
	private static final ShardLibrary serverShardLibrary = new ShardLibraryImpl();
	private static final ShardLibrary clientShardLibrary = null;
	private static Thread serverThread = null;
	private static Thread clientThread = null;
	
	public static ShardLibrary getServerLibrary() {
		if (serverThread != null && !Thread.currentThread().equals(serverThread)) {
			throw new IllegalStateException("getServerLibrary called from thread '"+Thread.currentThread().getName()+"'. This method can only be accessed from the server thread.");
		}
		
		return serverShardLibrary;
	}
	
	@Environment(EnvType.CLIENT)
	public static ShardLibrary getClientLibrary() {
		if (clientThread != null && !Thread.currentThread().equals(clientThread)) {
			throw new IllegalStateException("getClientLibrary called from thread '"+Thread.currentThread().getName()+"'. This method can only be accessed from the client thread.");
		}
		
		return clientShardLibrary;
	}
	
	@Deprecated(forRemoval = true)
	public static Multimap<Identifier, Shard> getShardSets() {
		return ScatteredShardsAPIImpl.shardSets;
	}

	@Deprecated(forRemoval = true)
	public static BiMap<Identifier, Shard> getShardData() {
		return ScatteredShardsAPIImpl.shardData;
	}

	@Deprecated(forRemoval = true)
	public static BiMap<Identifier, ShardType> getShardTypes() {
		return ScatteredShardsAPIImpl.shardTypes;
	}

	@Deprecated(forRemoval = true)
	public static void registerShardType(Identifier id, ShardType shardType) {
		ScatteredShardsAPIImpl.REGISTERED_SHARD_TYPES.put(id, shardType);
	}

	/**
	 * This just forwards the collect down to the ShardCollectionComponent
	 * @see ShardCollectionComponent#addShard(Identifier)
	 */
	@Deprecated()
	public static void triggerShardCollection(ServerPlayerEntity player, Identifier shardId) {
		//TODO: Move to ShardCollection
		ShardCollectionComponent collection = ScatteredShardsComponents.COLLECTION.get(player);
		collection.addShard(shardId);
	}
	
	public static void init() {
		
	}
}
