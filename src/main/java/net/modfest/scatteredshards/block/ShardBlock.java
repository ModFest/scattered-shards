package net.modfest.scatteredshards.block;

import java.util.Optional;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

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

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}

	@Override
	public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
		return SHAPE;
	}
	
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (world.isClient) return;
		if (entity instanceof PlayerEntity player) {
			Optional<ShardBlockEntity> be = world.getBlockEntity(pos, ScatteredShardsContent.SHARD_BLOCKENTITY);
			Identifier shardId = be.map(it -> it.shardId).orElse(null);
			if (shardId != null) {
				//Make sure the shard exists and the player doesn't have it before awarding it!
				ShardCollectionComponent collection = ScatteredShardsComponents.COLLECTION.get(player);
				if (!collection.contains(shardId)) {
					ShardLibraryComponent library = ScatteredShardsComponents.getShardLibrary(world);
					if (library.contains(shardId)) {
						//Collect it!
						collection.addShard(shardId);
					}
				}
			}
		}
	}
}
