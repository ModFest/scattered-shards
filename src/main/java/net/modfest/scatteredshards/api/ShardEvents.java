package net.modfest.scatteredshards.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardEvents {

	public static final Event<Collect> COLLECT = EventFactory.createArrayBacked(Collect.class, (handlers) -> (player, shardId, shard) -> {
		for (Collect handler : handlers) {
			handler.handle(player, shardId, shard);
		}
	});

	@FunctionalInterface
	public static interface Collect {
		public void handle(ServerPlayerEntity player, Identifier shardId, Shard shard);
	}

}
