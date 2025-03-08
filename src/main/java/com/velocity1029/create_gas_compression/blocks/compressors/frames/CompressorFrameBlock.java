package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.block.IBE;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class CompressorFrameBlock extends DirectionalKineticBlock implements IBE<CompressorFrameBlockEntity> {


//    static public CompressorFrameBlockEntity blockEntity;

    public CompressorFrameBlock(Properties properties) {
        super(properties);
    }

//    @Override
//    public CompressorFrameBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        blockEntity = new CompressorFrameBlockEntity(getBlockEntityType(), pos, state);
//        return blockEntity;
//    }

    public static boolean isFrame(BlockState state) {
        return state.getBlock() instanceof CompressorFrameBlock;
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(FACING)
                .getAxis();
    }

    @Override
    public Class<CompressorFrameBlockEntity> getBlockEntityClass() {
        return CompressorFrameBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompressorFrameBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_FRAME.get();
    }
}
