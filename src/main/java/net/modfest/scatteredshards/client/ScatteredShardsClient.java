package net.modfest.scatteredshards.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;

import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.command.ClientShardCommand;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class ScatteredShardsClient implements ClientModInitializer {

	public static final String SHARD_MODIFY_TOAST_KEY = "toast.scattered_shards.shard_mod";

	@Override
	public void onInitializeClient() {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
		ScatteredShardsAPI.initClient();
	}
	
	public static void triggerShardCollectAnimation(Identifier shardId) {
		var library = ScatteredShardsAPI.getClientLibrary();
		var collection = ScatteredShardsAPI.getClientCollection();
		
		Shard shard = library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
		if (shard == null) {
			ScatteredShards.LOGGER.warn("Received shard collection event with ID '" + shardId + "' but it does not exist on this client");
			return;
		}
		
		collection.add(shardId);
		ScatteredShards.LOGGER.info("Collected shard '" + shardId.toString() + "'!");

		library.shardTypes()
			.get(shard.shardTypeId())
			.flatMap(ShardType::collectSound)
			.ifPresent((sound) -> {
				MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F, 0.8F));
			});

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

	public static void openShardTablet() {
		final var client = MinecraftClient.getInstance();
		client.send(() -> {
			final var library = ScatteredShardsAPI.getClientLibrary();
			final var collection = ScatteredShardsAPI.getClientCollection();

			client.setScreen(new ShardTabletGuiDescription.Screen(collection, library));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f));
		});
	}
}
