package net.modfest.scatteredshards;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.component.ComponentType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.block.ShardBlock;
import net.modfest.scatteredshards.block.ShardBlockEntity;
import net.modfest.scatteredshards.client.render.ShardBlockEntityRenderer;
import net.modfest.scatteredshards.item.ShardItem;
import net.modfest.scatteredshards.item.ShardTablet;

import java.util.function.Function;

public class ScatteredShardsContent {
	public static final Block SHARD_BLOCK = registerBlock(ShardBlock::new, ShardBlock.SETTINGS, "shard_block", true);

	public static final Item SHARD_TABLET = registerItem(ShardTablet::new, new Item.Settings(), "shard_tablet");
	public static final Item SHARD_ITEM = registerItem(ShardItem::new, new Item.Settings(), "shard_item");

	public static final BlockEntityType<ShardBlockEntity> SHARD_BLOCKENTITY = registerBlockEntity("shard_block", ShardBlockEntity::new, SHARD_BLOCK);

	public static final ComponentType<Identifier> SHARD_ID_COMPONENT = Registry.register(
		Registries.DATA_COMPONENT_TYPE,
		ScatteredShards.id("shard_id"),
		ComponentType.<Identifier>builder().codec(Identifier.CODEC).build()
	);


	private static Item registerItem(Function<Item.Settings, Item> factory, Item.Settings settings, String path) {
		var location = ScatteredShards.id(path);
		var key = RegistryKey.of(RegistryKeys.ITEM, location);

		return Items.register(key, factory, settings);
	}

	private static <T extends Block> T registerBlock(Function<AbstractBlock.Settings, T> blockFactory, AbstractBlock.Settings settings, String path, boolean shouldRegisterItem) {
		var blockKey = RegistryKey.of(RegistryKeys.BLOCK, ScatteredShards.id(path));
		var block = blockFactory.apply(settings.registryKey(blockKey));

		if (shouldRegisterItem) {
			var itemKey = RegistryKey.of(RegistryKeys.ITEM, ScatteredShards.id(path));

			var blockItem = new BlockItem(block, new Item.Settings().registryKey(itemKey));
			Registry.register(Registries.ITEM, itemKey, blockItem);
		}

		return Registry.register(Registries.BLOCK, blockKey, block);
	}

	private static <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(
		String path,
		FabricBlockEntityTypeBuilder.Factory<? extends T> entityFactory,
		Block... blocks
	) {
		var location = ScatteredShards.id(path);
		return Registry.register(Registries.BLOCK_ENTITY_TYPE, location, FabricBlockEntityTypeBuilder.<T>create(entityFactory, blocks).build());
	}

	public static void register() {
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		BlockEntityRendererFactories.register(ScatteredShardsContent.SHARD_BLOCKENTITY, ShardBlockEntityRenderer::new);
	}
}
