package net.modfest.scatteredshards.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;

import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.command.ClientShardCommand;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ScatteredShardsClient implements ClientModInitializer {

	public static final String SHARD_MODIFY_TOAST_KEY = "toast.scattered_shards.shard_mod";

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
	}

	public static void triggerShardCollectAnimation(Identifier shardId) {
		var library = ScatteredShardsComponents.getShardLibrary(MinecraftClient.getInstance().world);
		var collection = ScatteredShardsComponents.COLLECTION.get(MinecraftClient.getInstance().player);
		
		Shard shard = library.getShard(shardId);
		if (shard == null) {
			ScatteredShards.LOGGER.warn("Received shard collection event with ID '" + shardId + "' but it does not exist on this client");
			return;
		}
		
		collection.addShard(shardId);
		ScatteredShards.LOGGER.info("Collected shard '" + shardId.toString() + "'!");
		Toast toast = new ShardToast(shard);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}

	public static void triggerShardModificationToast(Identifier shardId, boolean success) {
		var toast = new SystemToast(
				SystemToast.Type.TUTORIAL_HINT,
				Text.translatable(SHARD_MODIFY_TOAST_KEY + ".title"),
				Text.translatable(SHARD_MODIFY_TOAST_KEY + "." + (success ? "success" : "fail"), shardId)
		);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}
}
