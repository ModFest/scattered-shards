package net.modfest.scatteredshards.item;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class ShardItem extends Item {
	public ShardItem(Settings settings) {
		super(settings);
	}


	/**
	 * Creates a shard item
	 *
	 * @return the shard item
	 */
	public static ItemStack createShardItem(Identifier shardId, Text name) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_ITEM);
		stack.set(ScatteredShardsContent.SHARD_ID_COMPONENT, shardId);
		stack.set(DataComponentTypes.ITEM_NAME, name);

		return stack;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
		if (world.isClient || !(entity instanceof ServerPlayerEntity player) || player.isInCreativeMode()) return;

		Identifier id = stack.get(ScatteredShardsContent.SHARD_ID_COMPONENT);
		stack.setCount(0);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Optional<Shard> toCollect = library.shards().get(id);
		if (toCollect.isEmpty()) return;

		ScatteredShardsAPI.triggerShardCollection(player, id);
	}

	@Override
	public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
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
		Text shardTypeDesc = ShardType.getDescription(shardTypeId).copy().fillStyle(Style.EMPTY.withColor(0xFF_000000 | shardType.textColor()));

		textConsumer.accept(shardTypeDesc);
		textConsumer.accept(Text.translatable("item.scattered_shards.shard_item.description").formatted(Formatting.GRAY));
	}
}
