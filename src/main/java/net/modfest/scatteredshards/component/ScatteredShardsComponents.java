package net.modfest.scatteredshards.component;

import com.mojang.brigadier.context.CommandContext;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.world.World;
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
				ShardLibraryComponent::new
				);
	}

	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(
				ScatteredShardsComponents.COLLECTION,
				ShardCollectionComponent::new,
				RespawnCopyStrategy.ALWAYS_COPY
				);
	}
	
	/**
	 * Convenience method to get the global shard library for your Side
	 */
	public static ShardLibraryComponent getShardLibrary(World world) {
		return LIBRARY.get(world.getProperties());
	}
	
	public static ShardLibraryComponent getShardLibrary(CommandContext<ServerCommandSource> ctx) {
		return getShardLibrary(ctx.getSource().getWorld());
	}
	
	public static ShardLibraryComponent getShardLibrary(ServerCommandSource commandSource) {
		return getShardLibrary(commandSource.getWorld());
	}
	
	/**
	 * Convenience method to get a player's shard collection
	 */
	public static ShardCollectionComponent getShardCollection(PlayerEntity player) {
		return COLLECTION.get(player);
	}
}
