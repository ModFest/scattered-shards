package net.modfest.scatteredshards;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.command.ShardCommand;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatteredShards implements ModInitializer {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);

	public static Identifier id(String path) {
		return new Identifier(ID, path);
	}
	
	public static String permission(String path) {
		return ID + "." + path;
	}

	@Override
	public void onInitialize() {
		ShardType.register();
		ShardTypeLoader.register();
		ShardSetLoader.register();
		ShardCommand.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register();
		FabricLoader.getInstance().getModContainer(ID).ifPresent(mod -> {
			ResourceManagerHelper.registerBuiltinResourcePack(ScatteredShards.id("test"), mod, ResourcePackActivationType.NORMAL);
		});
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			//Debug handler - we want to know the state of the shard library PersistentState after the player has connected.
			
			ShardLibraryPersistentState.get(server); //This should trigger some Stuff
		});
	}
}
