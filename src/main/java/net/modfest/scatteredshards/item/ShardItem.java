package net.modfest.scatteredshards.item;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

import java.util.List;
import java.util.Optional;

public class ShardItem extends Item {
	public ShardItem(Settings settings) {
		super(settings);
	}


	/**
	 * Creates a shard item
	 *
	 * @return the shard item
	 */
	public static ItemStack createShardItem(Identifier shardId) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_ITEM);
		stack.set(ScatteredShardsContent.SHARD_ID_COMPONENT, shardId);

		return stack;
	}

	@Override
	public void inventoryTick(ItemStack itemStack, World world, Entity entity, int slot, boolean selected) {
		if (world.isClient || !(entity instanceof ServerPlayerEntity player) || player.isInCreativeMode()) return;

		Identifier id = itemStack.get(ScatteredShardsContent.SHARD_ID_COMPONENT);
		player.getInventory().setStack(slot, ItemStack.EMPTY);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Optional<Shard> toCollect = library.shards().get(id);
		if (toCollect.isEmpty()) return;

		ScatteredShardsAPI.triggerShardCollection(player, id);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
		Identifier id = stack.get(ScatteredShardsContent.SHARD_ID_COMPONENT);

		if (id == null) {
			return;
		}

		ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
		Optional<Shard> shard = library.shards().get(id);

		if (shard.isEmpty()) {
			return;
		}

		Identifier shardTypeId = shard.get().shardTypeId();
		ShardType shardType = library.shardTypes().get(shardTypeId).orElse(ShardType.MISSING);
		Text shardTypeDesc = ShardType.getDescription(shardTypeId).copy().fillStyle(Style.EMPTY.withColor(shardType.textColor()));

		tooltip.add(shardTypeDesc);
		tooltip.add(Text.translatable("item.scattered_shards.shard_item.description").formatted(Formatting.GRAY));
	}

	@Override
	public Text getName(ItemStack stack) {
		Identifier id = stack.get(ScatteredShardsContent.SHARD_ID_COMPONENT);

		if (id == null) {
			return getName();
		}

		ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
		Optional<Shard> shard = library.shards().get(id);

		if (shard.isEmpty()) {
			return getName();
		}

		return shard.get().name();
	}
}
