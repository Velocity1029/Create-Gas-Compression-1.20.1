package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

public class GlassIronPipeBlock extends GlassFluidPipeBlock {
    public GlassIronPipeBlock(Properties p_i48339_1_) {
        super(p_i48339_1_);
    }


    @Override
    public BlockState toRegularPipe(LevelAccessor world, BlockPos pos, BlockState state) {
        Direction side = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
        Map<Direction, BooleanProperty> facingToPropertyMap = IronPipeBlock.PROPERTY_BY_DIRECTION;
        return CGCBlocks.IRON_PIPE.get()
                .updateBlockState(CGCBlocks.IRON_PIPE.getDefaultState()
                        .setValue(facingToPropertyMap.get(side), true)
                        .setValue(facingToPropertyMap.get(side.getOpposite()), true), side, null, world, pos);
    }
}
