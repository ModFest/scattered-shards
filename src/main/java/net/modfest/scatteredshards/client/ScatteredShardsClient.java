package net.modfest.scatteredshards.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.util.InputUtil;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.command.ClientShardCommand;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;

public class ScatteredShardsClient implements ClientModInitializer {
	public static final KeyBinding VIEW_COLLECTION = KeyBindingHelper.registerKeyBinding(new KeyBinding(
		"key.scattered_shards.collection",
		InputUtil.UNKNOWN_KEY.getCode(),
		"key.categories.scattered_shards"
	));

	@Override
	public void onInitializeClient() {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
		ScatteredShardsAPI.initClient();
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (VIEW_COLLECTION.wasPressed()) {
				openShardTablet();
			}
		});
	}

	public static void onShardCollected(Identifier shardId) {
		var library = ScatteredShardsAPI.getClientLibrary();
		var collection = ScatteredShardsAPI.getClientCollection();

		Shard shard = library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
		if (shard == Shard.MISSING_SHARD) {
			ScatteredShards.LOGGER.warn("Received shard collection event with ID '{}' but it does not exist on this client", shardId);
			return;
		}

		ShardTabletGuiDescription.INITIAL_SHARD = shardId;
		ShardTabletGuiDescription.INITIAL_SCROLL_POSITION = -1;

		collection.add(shardId);
		ScatteredShards.LOGGER.info("Collected shard '{}'!", shardId.toString());

		library.shardTypes()
			.get(shard.shardTypeId())
			.flatMap(ShardType::collectSound)
			.ifPresent((sound) -> MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(sound, 1.0F, 0.8F)));

		Toast toast = new ShardCollectedToast(shard);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}

	public static void triggerShardModificationToast(Identifier shardId, boolean success) {
		var toast = new SystemToast(
			SystemToast.Type.PERIODIC_NOTIFICATION,
			Text.translatable("toast.scattered_shards.shard_mod.title"),
			Text.stringifiedTranslatable(success ? "toast.scattered_shards.shard_mod.success" : "toast.scattered_shards.shard_mod.success.fail", shardId)
		);
		MinecraftClient.getInstance().getToastManager().add(toast);
	}

	public static void openShardTablet() {
		MinecraftClient.getInstance().send(() -> {
			final ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			final ShardCollection collection = ScatteredShardsAPI.getClientCollection();

			MinecraftClient.getInstance().setScreen(new ShardTabletGuiDescription.Screen(collection, library));
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f));
		});
	}

	public static boolean hasShiftDown() {
		return InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 340) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), 344);
	}
}
