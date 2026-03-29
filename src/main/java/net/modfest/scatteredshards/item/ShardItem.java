package net.modfest.scatteredshards.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class ShardItem extends Item {
	public ShardItem(Properties settings) {
		super(settings);
	}


	/**
	 * Creates a shard item
	 *
	 * @return the shard item
	 */
	public static ItemStack createShardItem(Identifier shardId, Component name) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_ITEM);
		stack.set(ScatteredShardsContent.SHARD_ID_COMPONENT, shardId);
		stack.set(DataComponents.ITEM_NAME, name);

		return stack;
	}

	@Override
	public void inventoryTick(ItemStack stack, ServerLevel world, Entity entity, @Nullable EquipmentSlot slot) {
		if (world.isClientSide() || !(entity instanceof ServerPlayer player) || player.hasInfiniteMaterials()) return;

		Identifier id = stack.get(ScatteredShardsContent.SHARD_ID_COMPONENT);
		stack.setCount(0);

		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Optional<Shard> toCollect = library.shards().get(id);
		if (toCollect.isEmpty()) return;

		ScatteredShardsAPI.triggerShardCollection(player, id);
	}

	@Override
	public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay displayComponent, Consumer<Component> textConsumer, TooltipFlag type) {
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
		Component shardTypeDesc = ShardType.getDescription(shardTypeId).copy().withStyle(Style.EMPTY.withColor(0xFF_000000 | shardType.textColor()));

		textConsumer.accept(shardTypeDesc);
		textConsumer.accept(Component.translatable("item.scattered_shards.shard_item.description").withStyle(ChatFormatting.GRAY));
	}
}
