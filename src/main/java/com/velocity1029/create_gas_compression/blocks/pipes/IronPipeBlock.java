package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import net.minecraft.world.level.block.state.BlockState;

public class IronPipeBlock extends FluidPipeBlock {
    public IronPipeBlock(Properties properties) {
        super(properties);
    }

    public static boolean isPressurizedPipe(BlockState state) {
        return state.getBlock() instanceof IronPipeBlock;
    }


}
