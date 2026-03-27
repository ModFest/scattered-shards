package net.modfest.scatteredshards.api;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Objects;

public class GlobalCollection {
	int totalPlayers;
	HashMap<ResourceLocation, Integer> collectionTracker;

	public GlobalCollection(int totalPlayers, HashMap<ResourceLocation, Integer> collectionTracker) {
		this.totalPlayers = totalPlayers;
		this.collectionTracker = collectionTracker;
	}

	public int totalPlayers() {
		return totalPlayers;
	}

	public HashMap<ResourceLocation, Integer> collectionTracker() {
		return collectionTracker;
	}

	public static final StreamCodec<RegistryFriendlyByteBuf, GlobalCollection> PACKET_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, GlobalCollection::totalPlayers,
		ByteBufCodecs.map(HashMap::new, ResourceLocation.STREAM_CODEC, ByteBufCodecs.VAR_INT), GlobalCollection::collectionTracker,
		GlobalCollection::new
	);

	public int getCount(ResourceLocation shard) {
		return Objects.requireNonNullElse(collectionTracker.get(shard), 0);
	}

	public void update(ResourceLocation shard, int change, int playerCount) {
		collectionTracker.compute(shard, (k, count) -> count != null ? count + change : 1);
		totalPlayers = playerCount;
	}
}
