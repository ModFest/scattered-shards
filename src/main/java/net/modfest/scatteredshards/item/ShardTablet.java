package net.modfest.scatteredshards.item;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.Level;
import net.modfest.scatteredshards.client.ScatteredShardsClient;

public class ShardTablet extends Item {

	public ShardTablet(Properties settings) {
		super(settings);
	}

	@Environment(EnvType.CLIENT)
	// TODO: sneak interact on another player opens their collection
	@Override
	public InteractionResult use(Level world, Player user, InteractionHand hand) {
		ScatteredShardsClient.openShardTablet();
		return InteractionResult.SUCCESS;
	}
}
