package net.modfest.scatteredshards.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ShardBlock extends Block implements BlockEntityProvider {
	public static final VoxelShape SHAPE = VoxelShapes.cuboid(4 / 16f, 3 / 16f, 4 / 16f, 12 / 16f, 13 / 16f, 12 / 16f);
	private static final Block.Settings SETTINGS = Block.Settings.create()
		.dropsNothing()
		.noCollision()
		.nonOpaque()
		.luminance(state -> 3)
		.strength(-1)
		.mapColor(MapColor.EMERALD_GREEN);

	public ShardBlock() {
		super(SETTINGS);
	}

	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new ShardBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		if (world.isClient() && type == ScatteredShardsContent.SHARD_BLOCKENTITY) {
			return ShardBlockEntity::clientTick;
		}

		return null;
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}

	public static boolean tryCollect(World world, PlayerEntity player, ShardBlockEntity be) {
		// Make sure the shard exists before awarding it!
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Optional<Shard> toCollect = library.shards().get(be.shardId);
		if (toCollect.isEmpty()) {
			return false;
		}

		if (player instanceof ServerPlayerEntity serverPlayer) {
			return ScatteredShardsAPI.triggerShardCollection(serverPlayer, be.shardId);
		} else {
			return false;
		}
	}

	@Override
	protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof ShardBlockEntity be) || !be.canInteract) {
			return ActionResult.PASS;
		}
		if (world.isClient) {
			return ActionResult.CONSUME;
		}
		if (tryCollect(world, player, be)) {
			return ActionResult.SUCCESS;
		}
		return ActionResult.FAIL;
	}

	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient || !(entity instanceof PlayerEntity player)) {
			return;
		}
		if (world.getBlockEntity(pos) instanceof ShardBlockEntity be) {
			tryCollect(world, player, be);
		}
	}
	
	@Override
	public ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state) {
		
		BlockEntity entity = world.getBlockEntity(pos);
		if (world.isClient() && entity instanceof ShardBlockEntity shardEntity) {
			Identifier shardId = shardEntity.getShardId();
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			return createShardBlock(library, shardId, shardEntity.canInteract(), shardEntity.getGlowSize(), shardEntity.getGlowStrength());
		} else {
			
			return super.getPickStack(world, pos, state);
		}
	}
	
	/**
	 * Creates a shard block
	 *
	 * @return the shard block
	 */
	public static ItemStack createShardBlock(ShardLibrary library, Identifier shardId, boolean canInteract, float glowSize, float glowStrength) {
		Shard shard = library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
		ShardType shardType = library.shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);
		return createShardBlock(shardType, shardId, shard, canInteract, glowSize, glowStrength);
		/*
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_BLOCK);

		NbtCompound blockEntityTag = new NbtCompound();
		blockEntityTag.putString("id", ScatteredShardsContent.SHARD_BLOCK_ID.toString()); // required, see NbtComponent.CODEC_WITH_ID
		blockEntityTag.putString("Shard", shardId.toString());

		//Fill in name / lore
		Shard shard = library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
		stack.set(DataComponentTypes.ITEM_NAME, shard.name());
		ShardType shardType = library.shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);
		Text shardTypeDesc = ShardType.getDescription(shard.shardTypeId()).copy().fillStyle(Style.EMPTY.withColor(shardType.textColor()));
		LoreComponent lore = new LoreComponent(List.of(shardTypeDesc));
		stack.set(DataComponentTypes.LORE, lore);

		blockEntityTag.putBoolean("CanInteract", canInteract);

		NbtCompound glowTag = new NbtCompound();
		glowTag.putFloat("size", glowSize);
		glowTag.putFloat("strength", glowStrength);
		blockEntityTag.put("Glow", glowTag);

		stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(blockEntityTag));

		return stack;*/
	}
	
	public static ItemStack createShardBlock(ShardType shardType, Identifier shardId, Shard shard, boolean canInteract, float glowSize, float glowStrength) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_BLOCK);
		
		NbtCompound blockEntityTag = new NbtCompound();
		blockEntityTag.putString("id", ScatteredShardsContent.SHARD_BLOCK_ID.toString()); // required, see NbtComponent.CODEC_WITH_ID
		blockEntityTag.putString("Shard", shardId.toString());

		//Fill in name / lore
		stack.set(DataComponentTypes.ITEM_NAME, shard.name());
		Text shardTypeDesc = ShardType.getDescription(shard.shardTypeId()).copy().fillStyle(Style.EMPTY.withColor(shardType.textColor()));
		LoreComponent lore = new LoreComponent(List.of(shardTypeDesc));
		stack.set(DataComponentTypes.LORE, lore);

		blockEntityTag.putBoolean("CanInteract", canInteract);

		NbtCompound glowTag = new NbtCompound();
		glowTag.putFloat("size", glowSize);
		glowTag.putFloat("strength", glowStrength);
		blockEntityTag.put("Glow", glowTag);

		stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(blockEntityTag));

		return stack;
	}
}
