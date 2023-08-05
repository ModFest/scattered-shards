package net.modfest.scatteredshards.client;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.command.ClientShardCommand;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ScatteredShardsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
	}

	public static void triggerShardCollectAnimation(Identifier shardId) {
		Shard shard = ScatteredShardsAPI.getShardData().get(shardId);
		if (shard == null) {
			ScatteredShards.LOGGER.warn("Received shard collection event with ID '" + shardId + "' but it does not exist on this client");
			return;
		}

		ScatteredShards.LOGGER.info("Collected shard '" + shardId.toString() + "'!");

		//TODO: Activate the HUD overlay
	}
}
