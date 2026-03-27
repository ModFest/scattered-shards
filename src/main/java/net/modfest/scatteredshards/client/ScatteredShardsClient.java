package net.modfest.scatteredshards.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
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
	public static final KeyMapping VIEW_COLLECTION = KeyMappingHelper.registerKeyBinding(new KeyMapping(
		"key.scattered_shards.collection",
		InputConstants.UNKNOWN.getValue(),
		"key.categories.scattered_shards"
	));

	@Override
	public void onInitializeClient() {
		ClientShardCommand.register();
		ScatteredShardsNetworking.registerClient();
		ScatteredShardsContent.registerClient();
		ScatteredShardsAPI.initClient();
		ClientTickEvents.END_CLIENT_TICK.register(c -> {
			if (VIEW_COLLECTION.consumeClick()) {
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
			.ifPresent((sound) -> Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0F, 0.8F)));

		Toast toast = new ShardCollectedToast(shard);
		Minecraft.getInstance().getToastManager().addToast(toast);
	}

	public static void triggerShardModificationToast(Identifier shardId, boolean success) {
		var toast = new SystemToast(
			SystemToast.SystemToastId.PERIODIC_NOTIFICATION,
			Component.translatable("toast.scattered_shards.shard_mod.title"),
			Component.translatableEscape(success ? "toast.scattered_shards.shard_mod.success" : "toast.scattered_shards.shard_mod.success.fail", shardId)
		);
		Minecraft.getInstance().getToastManager().addToast(toast);
	}

	public static void openShardTablet() {
		Minecraft.getInstance().schedule(() -> {
			final ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			final ShardCollection collection = ScatteredShardsAPI.getClientCollection();

			Minecraft.getInstance().setScreen(new ShardTabletGuiDescription.Screen(collection, library));
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0f, 1.0f));
		});
	}

	public static boolean hasShiftDown() {
		return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 340) || InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), 344);
	}
}
