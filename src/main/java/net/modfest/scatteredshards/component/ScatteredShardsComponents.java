package net.modfest.scatteredshards.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import net.minecraft.entity.player.PlayerEntity;
import net.modfest.scatteredshards.ScatteredShards;

public class ScatteredShardsComponents implements EntityComponentInitializer {
	public static final ComponentKey<ShardCollectionComponent> COLLECTION = 
			ComponentRegistry.getOrCreate(ScatteredShards.id("collection"), ShardCollectionComponent.class);
	
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(
				ScatteredShardsComponents.COLLECTION,
				ShardCollectionComponent::new,
				RespawnCopyStrategy.ALWAYS_COPY
				);
	}
	
	/**
	 * Convenience method to get a player's shard collection
	 */
	public static ShardCollectionComponent getShardCollection(PlayerEntity player) {
		return COLLECTION.get(player);
	}
}
