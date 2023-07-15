package net.modfest.scatteredshards;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.ShardType;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import net.modfest.scatteredshards.core.ScatteredShardsContent;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatteredShards implements ModInitializer {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	
	public static Identifier id(String path) {
		return new Identifier(ID, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ShardType.register();
		ShardTypeLoader.register();
		ShardSetLoader.register();
		ScatteredShardsNetworking.register();
		ScatteredShardsContent.register();
	}
}
