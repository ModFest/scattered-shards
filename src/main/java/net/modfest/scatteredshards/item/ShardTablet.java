package net.modfest.scatteredshards.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;

public class ShardTablet extends Item {

	public ShardTablet(Settings settings) {
		super(settings);
	}

	@Environment(EnvType.CLIENT)
	// TODO: sneak interact on another player opens their collection
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		final var client = MinecraftClient.getInstance();
		client.send(() -> {
			final var library = ScatteredShardsAPI.getClientLibrary();
			final var collection = ScatteredShardsAPI.getClientCollection();
			
			client.setScreen(new ShardTabletGuiDescription.Screen(collection, library));
			client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f, 1.0f));
		});
		return TypedActionResult.success(user.getStackInHand(hand));
	}
}
