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
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
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
	
	public Stream<Identifier> streamIds() {
		return collection.stream();
	}
	
	public Stream<Shard> stream() {
		return collection.stream().map(it -> ScatteredShardsAPI.getShardData().getOrDefault(it, Shard.MISSING_SHARD));
	}
	
	@Override
	public Iterator<Identifier> iterator() {
		return collection.iterator();
	}
	
	public void addShard(Identifier shardId) {
		collection.add(shardId);
		if (provider instanceof ServerPlayerEntity serverPlayer) {
			ScatteredShardsNetworking.s2cCollectShard(serverPlayer, shardId);
		}
	}
	
	public void removeShard(Identifier shardId) {
		collection.remove(shardId);
		ScatteredShardsComponents.COLLECTION.sync(provider); //TODO: Send a smaller packet just containing the shard removed
	}
	
	@Override
	public void readFromNbt(NbtCompound tag) {
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
