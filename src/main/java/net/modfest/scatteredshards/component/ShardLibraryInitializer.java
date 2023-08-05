package net.modfest.scatteredshards.component;

import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;

public class ShardLibraryInitializer implements LevelComponentInitializer {

	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
		registry.register(ScatteredShardsComponents.LIBRARY, ShardLibraryComponent.class, it -> new ShardLibraryComponent());
	}

}
