package net.modfest.scatteredshards.component;

import java.util.Set;
import java.util.stream.Stream;

import java.util.HashSet;
import java.util.Iterator;

import dev.onyxstudios.cca.api.v3.component.Component;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ShardEvents;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

/**
 * Component on players which holds their shard collection.
 */
public class ShardCollectionComponent implements Component, Iterable<Identifier>, AutoSyncedComponent {
	public static final String COLLECTION_KEY = "Collection";
	
	protected final PlayerEntity provider;
	protected final Set<Identifier> collection = new HashSet<>();
	
	public ShardCollectionComponent(PlayerEntity provider) {
		this.provider = provider;
	}
	
	public boolean contains(Identifier shardId) {
		return collection.contains(shardId);
	}
	
	public Stream<Identifier> stream() {
		return collection.stream();
	}
	
	public int size() {
		return collection.size();
	}
	
	public void clear() {
		collection.clear();
		ScatteredShardsComponents.COLLECTION.sync(provider);
	}
	
	@Override
	public Iterator<Identifier> iterator() {
		return collection.iterator();
	}
	
	public boolean addShard(Identifier shardId) {
		if (collection.contains(shardId)) return false;
		
		collection.add(shardId);
		if (provider instanceof ServerPlayerEntity serverPlayer) {
			//Fire event
			Shard shard = ScatteredShardsComponents.getShardLibrary(provider.getWorld()).getShard(shardId);
			ShardEvents.COLLECT.invoker().handle(serverPlayer, shardId, shard);
			
			//Sync to client so they get the toast
			ScatteredShardsNetworking.s2cCollectShard(serverPlayer, shardId);
			return true;
		}

		return false;
	}
	
	public void removeShard(Identifier shardId) {
		boolean hadShard = collection.remove(shardId);

		if (hadShard && provider instanceof ServerPlayerEntity serverPlayer) {
			// Sync to client so shard will appear solid in-world again
			ScatteredShardsNetworking.s2cUncollectShard(serverPlayer, shardId);
		}
	}
	
	@Override
	public void readFromNbt(NbtCompound tag) {
		collection.clear();
		for(NbtElement elem : tag.getList(COLLECTION_KEY, NbtElement.STRING_TYPE)) {
			if (elem instanceof NbtString str) {
				collection.add(new Identifier(str.asString()));
			}
		}
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtList list = new NbtList();
		for(Identifier id : collection) {
			list.add(NbtString.of(id.toString()));
		}
		tag.put(COLLECTION_KEY, list);
	}

	@Override
	public boolean shouldSyncWith(ServerPlayerEntity player) {
		return (player.equals(provider));
	}

}
