package net.modfest.scatteredshards.api;

import org.quiltmc.qsl.base.api.event.Event;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.Shard;

public class ShardEvents {
	
	public static final Event<Collect> COLLECT = Event.create(Collect.class, (handlers) -> (player, shardId, shard) -> {
		for(Collect handler : handlers) {
			handler.handle(player, shardId, shard);
		}
	});
	
	@FunctionalInterface
	public static interface Collect {
		public void handle(ServerPlayerEntity player, Identifier shardId, Shard shard);
	}
	
}
