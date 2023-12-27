package net.modfest.scatteredshards.block;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

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
import net.minecraft.server.network.ServerPlayerEntity;
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
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.networking.S2CCollectShard;

public class ShardBlock extends Block implements BlockEntityProvider {
	public static final VoxelShape SHAPE = VoxelShapes.cuboid(4/16f, 3/16f, 4/16f, 12/16f, 13/16f, 12/16f);
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
		// Make sure the shard exists and the player doesn't have it before awarding it!
		ShardLibrary library = ScatteredShardsAPI.getServerLibrary(); 
		Optional<Shard> toCollect = library.shards().get(be.shardId);
		if (toCollect.isEmpty()) {
			return false;
		}
		
		//TODO: Implement new shard collection
		ShardCollection shardCollection = ScatteredShardsAPI.getServerCollection(player);
		
		if (shardCollection.add(be.shardId) && player instanceof ServerPlayerEntity serverPlayer) {
			S2CCollectShard.send(serverPlayer, be.shardId);
			
			return true;
		} else {
			return false;
		}
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
	
	/**
	 * Creates a shard block
	 * @return the shard block
	 */
	public static ItemStack createShardBlock(ShardLibrary library, Identifier shardId, boolean canInteract, float glowSize, float glowStrength) {
		ItemStack stack = new ItemStack(ScatteredShardsContent.SHARD_BLOCK);
		
		NbtCompound blockEntityTag = stack.getOrCreateSubNbt("BlockEntityTag");
		blockEntityTag.putString("Shard", shardId.toString());
		NbtCompound displayTag = stack.getOrCreateSubNbt("display");
		
		//Fill in name / lore
		Shard shard = library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
		displayTag.putString("Name", Text.Serialization.toJsonString(shard.name()));
		NbtList loreTag = new NbtList();
		displayTag.put("Lore", loreTag);
		ShardType shardType = library.shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);
		Text shardTypeDesc = ShardType.getDescription(shard.shardTypeId()).copy().fillStyle(Style.EMPTY.withColor(shardType.textColor()));
		loreTag.add(NbtString.of(
				Text.Serialization.toJsonString(shardTypeDesc)
				));

		blockEntityTag.putBoolean("CanInteract", canInteract);

		NbtCompound glowTag = new NbtCompound();
		glowTag.putFloat("size", glowSize);
		glowTag.putFloat("strength", glowStrength);
		blockEntityTag.put("Glow", glowTag);
		
		return stack;
	}
}
