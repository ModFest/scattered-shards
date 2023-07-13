package net.modfest.scatteredshards.client;

import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.command.ShardCommand;
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
}
