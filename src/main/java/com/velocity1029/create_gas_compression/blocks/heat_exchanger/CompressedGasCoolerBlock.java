package com.velocity1029.create_gas_compression.blocks.heat_exchanger;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.velocity1029.create_gas_compression.blocks.pipes.EncasedIronPipeBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.function.Supplier;

public class CompressedGasCoolerBlock extends EncasedIronPipeBlock {
    public CompressedGasCoolerBlock(Properties properties, Supplier<Block> casing) {
        super(properties, casing);
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSED_GAS_COOLER.get();
    }
}
