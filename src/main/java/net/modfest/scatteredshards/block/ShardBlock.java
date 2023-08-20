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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;
import org.jetbrains.annotations.Nullable;

public class ShardBlock extends Block implements BlockEntityProvider {
	public static final VoxelShape SHAPE = VoxelShapes.cuboid(4/16f, 3/16f, 4/16f, 12/16f, 13/16f, 12/16f);
	private static final Block.Settings SETTINGS = Block.Settings.create()
			.dropsNothing()
			.noCollision()
			.nonOpaque()
			.luminance(state -> 3)
			.mapColor(MapColor.EMERALD);

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
		// Make sure the shard exists and the player doesn't have it before awarding it!
		ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(world);
		if (!library.contains(be.shardId)) {
			return false;
		}
		ShardCollectionComponent collection = ScatteredShardsComponents.COLLECTION.get(player);
		return collection.addShard(be.shardId);
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (hand != Hand.MAIN_HAND || !(world.getBlockEntity(pos) instanceof ShardBlockEntity be) || !be.canInteract) {
			return ActionResult.PASS;
		}
		if (world.isClient) {
			return ActionResult.CONSUME;
		}
		if (tryCollect(world, player, be)) {
			return ActionResult.SUCCESS;
		}
		player.sendMessage(Text.translatable("block.scattered_shards.shard_block.pickup_fail"), true);
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
	
	public static ItemStack createShardBlock(ShardLibraryComponent library, Identifier shardId, boolean canInteract, float glowSize, float glowStrength) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_BLOCK);
		
		NbtCompound blockEntityTag = stack.getOrCreateSubNbt("BlockEntityTag");
		blockEntityTag.putString("Shard", shardId.toString());
		NbtCompound displayTag = stack.getOrCreateSubNbt("display");
		
		//Fill in name / lore
		Shard shard = library.getShard(shardId);
		displayTag.putString("Name", Text.Serializer.toJson(shard.name()));
		NbtList loreTag = new NbtList();
		displayTag.put("Lore", loreTag);
		ShardType shardType = shard.getShardType();
		Text shardTypeDesc = shardType.getDescription().copy().fillStyle(Style.EMPTY.withColor(shardType.textColor()));
		loreTag.add(NbtString.of(
				Text.Serializer.toJson(shardTypeDesc)
				));

		blockEntityTag.putBoolean("CanInteract", canInteract);

		NbtCompound glowTag = new NbtCompound();
		glowTag.putFloat("size", glowSize);
		glowTag.putFloat("strength", glowStrength);
		blockEntityTag.put("Glow", glowTag);
		
		return stack;
	}
}
