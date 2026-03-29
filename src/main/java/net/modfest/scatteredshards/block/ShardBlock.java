package net.modfest.scatteredshards.block;

import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ShardBlock extends Block implements EntityBlock {
	public static final VoxelShape SHAPE = Shapes.box(4 / 16f, 3 / 16f, 4 / 16f, 12 / 16f, 13 / 16f, 12 / 16f);
	public static final Block.Properties SETTINGS = Block.Properties.of()
		.noLootTable()
		.noCollision()
		.noOcclusion()
		.lightLevel(state -> 3)
		.strength(-1)
		.mapColor(MapColor.EMERALD);

	public ShardBlock(Block.Properties settings) {
		super(settings);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ShardBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		if (world.isClientSide() && type == ScatteredShardsContent.SHARD_BLOCKENTITY) {
			return ShardBlockEntity::clientTick;
		}

		return null;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	public static boolean tryCollect(Level world, Player player, ShardBlockEntity be) {
		// Make sure the shard exists before awarding it!
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary();
		Optional<Shard> toCollect = library.shards().get(be.shardId);
		if (toCollect.isEmpty()) {
			return false;
		}

		if (player instanceof ServerPlayer serverPlayer) {
			return ScatteredShardsAPI.triggerShardCollection(serverPlayer, be.shardId);
		} else {
			return false;
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
		if (!(world.getBlockEntity(pos) instanceof ShardBlockEntity be) || !be.canInteract) {
			return InteractionResult.PASS;
		}
		if (world.isClientSide()) {
			return InteractionResult.CONSUME;
		}
		if (tryCollect(world, player, be)) {
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.FAIL;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, boolean isPrecise) {
		if (level.isClientSide() || !(entity instanceof Player player)) {
			return;
		}
		if (level.getBlockEntity(pos) instanceof ShardBlockEntity be) {
			tryCollect(level, player, be);
		}
	}

	@Override
	protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
		BlockEntity entity = world.getBlockEntity(pos);
		if (world.isClientSide() && entity instanceof ShardBlockEntity shardEntity) {
			Identifier shardId = shardEntity.getShardId();
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();

			if (shardId == null || library == null) {
				return super.getCloneItemStack(world, pos, state, includeData);
			}

			return createShardBlock(library, shardId, shardEntity.canInteract(), shardEntity.getGlowSize(), shardEntity.getGlowStrength());
		} else {

			return super.getCloneItemStack(world, pos, state, includeData);
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

		CompoundTag blockEntityTag = new CompoundTag();
		blockEntityTag.putString("id", ScatteredShards.id("shard_block").toString()); // required, see NbtComponent.CODEC_WITH_ID
		blockEntityTag.putString("Shard", shardId.toString());

		//Fill in name / lore
		stack.set(DataComponents.ITEM_NAME, shard.name());
		Component shardTypeDesc = ShardType.getDescription(shard.shardTypeId()).copy().withStyle(Style.EMPTY.withColor(0xFF_000000 | shardType.textColor()));
		ItemLore lore = new ItemLore(List.of(shardTypeDesc));
		stack.set(DataComponents.LORE, lore);

		blockEntityTag.putBoolean("CanInteract", canInteract);

		CompoundTag glowTag = new CompoundTag();
		glowTag.putFloat("size", glowSize);
		glowTag.putFloat("strength", glowStrength);
		blockEntityTag.put("Glow", glowTag);

		stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(ScatteredShardsContent.SHARD_BLOCKENTITY, blockEntityTag));

		return stack;
	}
}
