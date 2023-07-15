package net.modfest.scatteredshards.core;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ShardBlockEntityRenderer;

public class ScatteredShardsContent {
	public static final Identifier SHARD_BLOCK_ID = ScatteredShards.id("shard_block");
	
	public static final Block SHARD_BLOCK = new ShardBlock();
	public static final Item SHARD_BLOCK_ITEM = new BlockItem(SHARD_BLOCK, new Item.Settings());
	public static final BlockEntityType<ShardBlockEntity> SHARD_BLOCKENTITY = 
			QuiltBlockEntityTypeBuilder.create(ShardBlockEntity::new, SHARD_BLOCK).build();
	
	public static void register() {
		Registry.register(Registries.BLOCK, SHARD_BLOCK_ID, SHARD_BLOCK);
		Registry.register(Registries.ITEM, SHARD_BLOCK_ID, SHARD_BLOCK_ITEM);
		Registry.register(Registries.BLOCK_ENTITY_TYPE, SHARD_BLOCK_ID, SHARD_BLOCKENTITY);
	}
	
	@ClientOnly
	public static void registerClient() {
		BlockEntityRendererFactories.register(ScatteredShardsContent.SHARD_BLOCKENTITY, ShardBlockEntityRenderer::new);
	}
}
