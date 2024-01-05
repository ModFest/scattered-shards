package net.modfest.scatteredshards.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

public class ShardTablet extends Item {

	public ShardTablet(Settings settings) {
		super(settings);
	}

	@Environment(EnvType.CLIENT)
	// TODO: sneak interact on another player opens their collection
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		ScatteredShardsClient.openShardTablet();
		return TypedActionResult.success(user.getStackInHand(hand));
	}
}
