package net.modfest.scatteredshards;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import net.modfest.scatteredshards.core.ShardBlock;
import net.modfest.scatteredshards.core.ShardBlockEntity;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScatteredShards implements ModInitializer {

	public static final String ID = "scattered_shards";

	public static final Logger LOGGER = LoggerFactory.getLogger(ID);
	
	public static Block SHARD_BLOCK = null;
	public static BlockEntityType<ShardBlockEntity> SHARD_BLOCKENTITY;
	public static Item SHARD_ITEM = null;
	
	public static Identifier id(String path) {
		return new Identifier(ID, path);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ShardSetLoader.register();
		ScatteredShardsNetworking.register();
		
		SHARD_BLOCK = new ShardBlock();
		Registry.register(Registries.BLOCK, id("shard"), SHARD_BLOCK);
		Registry.register(Registries.ITEM, id("shard_block"), new BlockItem(SHARD_BLOCK, new Item.Settings()));
		
		SHARD_BLOCKENTITY = QuiltBlockEntityTypeBuilder.create(ShardBlockEntity::new, SHARD_BLOCK).build();
		Registry.register(Registries.BLOCK_ENTITY_TYPE, id("shard"), SHARD_BLOCKENTITY);
	}
}
