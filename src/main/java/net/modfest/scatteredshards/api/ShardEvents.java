package net.modfest.scatteredshards.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardEvents {
	public static final Event<Collect> COLLECT = EventFactory.createArrayBacked(Collect.class, (handlers) -> (player, shardId, shard) -> {
		for (Collect handler : handlers) {
			handler.handle(player, shardId, shard);
		}
	});

	@FunctionalInterface
	public interface Collect {
		void handle(ServerPlayer player, ResourceLocation shardId, Shard shard);
	}

}
