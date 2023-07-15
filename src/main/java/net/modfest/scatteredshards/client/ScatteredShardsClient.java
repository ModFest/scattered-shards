package net.modfest.scatteredshards.client;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.command.ShardCommand;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ScatteredShardsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		ShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		
		BlockEntityRendererFactories.register(ScatteredShards.SHARD_BLOCKENTITY, ShardBlockEntityRenderer::new);
	}
	
	public static void triggerShardCollectAnimation(Identifier shardId) {
		Shard shard = ScatteredShardsAPI.getShardData().get(shardId);
		if (shard == null) {
			ScatteredShards.LOGGER.warn("Server notified us that we collected shard '" + shardId.toString() + "' but we don't know of that shard.");
			return;
		}
		
		ScatteredShards.LOGGER.info("Collected shard '" + shardId.toString() + "'!");
		
		
		//TODO: Activate the HUD overlay
	}
}
