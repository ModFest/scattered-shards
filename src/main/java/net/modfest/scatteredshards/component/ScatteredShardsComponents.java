package net.modfest.scatteredshards.component;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.modfest.scatteredshards.ScatteredShards;

public class ScatteredShardsComponents implements EntityComponentInitializer, LevelComponentInitializer {
	public static final ComponentKey<ShardCollectionComponent> COLLECTION = 
			ComponentRegistry.getOrCreate(ScatteredShards.id("collection"), ShardCollectionComponent.class);
	
	public static final ComponentKey<ShardLibraryComponent> LIBRARY = 
			ComponentRegistry.getOrCreate(ScatteredShards.id("library"), ShardLibraryComponent.class);

	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
		registry.register(
				ScatteredShardsComponents.LIBRARY,
				ShardLibraryComponent.class,
				it -> new ShardLibraryComponent()
				);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(
				ScatteredShardsComponents.COLLECTION,
				it -> new ShardCollectionComponent(),
				RespawnCopyStrategy.ALWAYS_COPY
				);
	}
}
