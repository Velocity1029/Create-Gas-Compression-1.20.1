package com.velocity1029.create_gas_compression.blocks.diffuser;

import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.velocity1029.create_gas_compression.blocks.pipes.EncasedIronPipeBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Supplier;

public class DiffuserBlock extends IronPipeBlock {
    public DiffuserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(super.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, false));
    }

    @Override
    public BlockEntityType<? extends IronPipeBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.DIFFUSER.get();
    }
}
