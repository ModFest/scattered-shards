package net.modfest.scatteredshards.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;

import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.command.ClientShardCommand;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

import java.util.Optional;

public class ScatteredShardsClient implements ClientModInitializer {

	public static final String SHARD_MODIFY_TOAST_KEY = "toast.scattered_shards.shard_mod";

	@Override
	public void onInitializeClient() {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
	}

	@SuppressWarnings("resource")
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

		Optional<SoundEvent> collectSound = shard.getShardType().collectSound();
		collectSound.ifPresent((it) -> MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(it, 1.0F, 0.8F)));

		Toast toast = new ShardToast(shard);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}

	public static void triggerShardModificationToast(Identifier shardId, boolean success) {
		var toast = new SystemToast(
				SystemToast.Type.PERIODIC_NOTIFICATION,
				Text.translatable(SHARD_MODIFY_TOAST_KEY + ".title"),
				Text.translatable(SHARD_MODIFY_TOAST_KEY + "." + (success ? "success" : "fail"), shardId)
		);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}
}
