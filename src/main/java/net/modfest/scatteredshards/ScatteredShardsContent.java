package net.modfest.scatteredshards;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.block.ShardBlock;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.client.render.ShardBlockEntityRenderer;
import net.modfest.scatteredshards.item.ShardItem;
import net.modfest.scatteredshards.item.ShardTablet;

import java.util.function.Function;

public class ScatteredShardsContent {
	public static final Block SHARD_BLOCK = registerBlock(ShardBlock::new, ShardBlock.SETTINGS, "shard_block", true);

	public static final Item SHARD_TABLET = registerItem(ShardTablet::new, new Item.Properties(), "shard_tablet");
	public static final Item SHARD_ITEM = registerItem(ShardItem::new, new Item.Properties(), "shard_item");

	public static final BlockEntityType<ShardBlockEntity> SHARD_BLOCKENTITY = registerBlockEntity("shard_block", ShardBlockEntity::new, SHARD_BLOCK);

	public static final DataComponentType<Identifier> SHARD_ID_COMPONENT = Registry.register(
		BuiltInRegistries.DATA_COMPONENT_TYPE,
		ScatteredShards.id("shard_id"),
		DataComponentType.<Identifier>builder().persistent(Identifier.CODEC).build()
	);


	private static Item registerItem(Function<Item.Properties, Item> factory, Item.Properties settings, String path) {
		var location = ScatteredShards.id(path);
		var key = ResourceKey.create(Registries.ITEM, location);
		var item =factory.apply(settings.setId(key));

		return Registry.register(BuiltInRegistries.ITEM, key, item);
	}

	private static <T extends Block> T registerBlock(Function<net.minecraft.world.level.block.state.BlockBehaviour.Properties, T> blockFactory, net.minecraft.world.level.block.state.BlockBehaviour.Properties settings, String path, boolean shouldRegisterItem) {
		var blockKey = ResourceKey.create(Registries.BLOCK, ScatteredShards.id(path));
		var block = blockFactory.apply(settings.setId(blockKey));

		if (shouldRegisterItem) {
			var itemKey = ResourceKey.create(Registries.ITEM, ScatteredShards.id(path));

			var blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
			Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);
		}

		return Registry.register(BuiltInRegistries.BLOCK, blockKey, block);
	}

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
        String path,
        FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
        Block... blocks
	) {
		var location = ScatteredShards.id(path);
		return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, location, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
	}

	public static void register() {
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		BlockEntityRenderers.register(ScatteredShardsContent.SHARD_BLOCKENTITY, ShardBlockEntityRenderer::new);
	}
}
