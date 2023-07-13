package net.modfest.scatteredshards.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class ShardBlock extends Block implements BlockEntityProvider {
	public static final VoxelShape SHAPE = VoxelShapes.cuboid(4/16f, 3/16f, 4/16f, 12/16f, 13/16f, 12/16f);
	
	public ShardBlock() {
		super(Block.Settings.create()
				.dropsNothing()
				.noCollision()
				.nonOpaque()
				.luminance(state -> 3)
				.mapColor(MapColor.EMERALD)
				);
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
}
