package com.velocity1029.create_gas_compression.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class Outcropping extends DirectionalBlock {

    public Outcropping(Properties properties) {
        super(properties);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.UP));
    }


    protected static final float AABB_MIN = 6.0F;
    protected static final float AABB_MAX = 10.0F;
    protected static final VoxelShape Y_AXIS_AABB = Block.box(5.0, 0.0, 7.0, 11.0, 16.0, 9.0);
    protected static final VoxelShape Z_AXIS_AABB = Block.box(5.0, 7.0, 0.0, 11.0, 9.0, 16.0);
    protected static final VoxelShape X_AXIS_AABB = Block.box(0.0, 7.0, 5.0, 16.0, 9.0, 11.0);



    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        Direction $$1 = pContext.getClickedFace();
        BlockState $$2 = pContext.getLevel().getBlockState(pContext.getClickedPos().relative($$1.getOpposite()));
        return $$2.is(this) && $$2.getValue(FACING) == $$1 ? (BlockState)this.defaultBlockState().setValue(FACING, $$1.getOpposite()) : (BlockState)this.defaultBlockState().setValue(FACING, $$1);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(new Property[]{FACING});
    }

    public PushReaction getPistonPushReaction(BlockState pState) {
        return PushReaction.NORMAL;
    }

    public VoxelShape getShape(BlockState state, BlockGetter p_154347_, BlockPos p_154348_, CollisionContext p_154349_) {
        switch (((Direction)state.getValue(FACING)).getAxis()) {
            case X:
            default:
                return X_AXIS_AABB;
            case Z:
                return Z_AXIS_AABB;
            case Y:
                return Y_AXIS_AABB;
        }
    }

    public BlockState rotate(BlockState state, Rotation rotation) {
        return (BlockState)state.setValue(FACING, rotation.rotate((Direction)state.getValue(FACING)));
    }

    public BlockState mirror(BlockState state, Mirror mirror) {
        return (BlockState)state.setValue(FACING, mirror.mirror((Direction)state.getValue(FACING)));
    }

}
