package com.velocity1029.create_gas_compression.blocks.engines.natural_gas;

import com.simibubi.create.AllShapes;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NaturalGasEngineBlock extends DirectionalKineticBlock implements IBE<NaturalGasEngineBlockEntity> {

    public NaturalGasEngineBlock(Properties properties) {
        super(properties);
        registerCapacity(defaultBlockState());
    }

    private void registerCapacity(BlockState state) {
        BlockStressValues.CAPACITIES.register(state.getBlock(), () -> {return 2048;});
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return AllShapes.MOTOR_BLOCK.get(state.getValue(FACING));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredFacing(context);
        if ((context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown()) || preferred == null)
            return super.getStateForPlacement(context);
        return defaultBlockState().setValue(FACING, preferred);
    }

    // IRotate:

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(FACING);
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
    }

    @Override
    public boolean hideStressImpact() {
        return true;
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<NaturalGasEngineBlockEntity> getBlockEntityClass() {
        return NaturalGasEngineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends NaturalGasEngineBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.NATURAL_GAS_ENGINE.get();
    }

}