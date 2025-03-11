package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.content.kinetics.base.DirectionalAxisKineticBlock;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class CompressorFrameBlock extends DirectionalAxisKineticBlock implements IBE<CompressorFrameBlockEntity>, ProperWaterloggedBlock {


//    static public CompressorFrameBlockEntity blockEntity;

    public CompressorFrameBlock(Properties properties) {
        super(properties);
//        registerDefaultState(stateDefinition.any().setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH)
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(WATERLOGGED));
    }

//    @Override
//    public CompressorFrameBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        blockEntity = new CompressorFrameBlockEntity(getBlockEntityType(), pos, state);
//        return blockEntity;
//    }

//    protected boolean prefersConnectionTo(LevelReader reader, BlockPos pos, Direction facing, boolean shaftAxis) {
//        if (!shaftAxis)
//            return false;
//        BlockPos neighbourPos = pos.relative(facing);
//        BlockState blockState = reader.getBlockState(neighbourPos);
//        Block block = blockState.getBlock();
//        return block instanceof IRotate
//                && ((IRotate) block).hasShaftTowards(reader, neighbourPos, blockState, facing.getOpposite());
//    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
//        return face.getAxis() == getRotationAxis(state);
        if (world.getBlockEntity(pos) instanceof CompressorFrameBlockEntity) {
            return face.getAxis() == getRotationAxis(state);
        } else {
            return face == state.getValue(FACING);
        }
    }

    public static boolean isFrame(BlockState state) {
        return state.getBlock() instanceof CompressorFrameBlock;
    }

//    @Override
//    public Direction.Axis getRotationAxis(BlockState state) {
//        return state.getValue(FACING)
//                .getAxis();
//    }

    @Override
    public Class<CompressorFrameBlockEntity> getBlockEntityClass() {
        return CompressorFrameBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompressorFrameBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_FRAME.get();
    }
}
