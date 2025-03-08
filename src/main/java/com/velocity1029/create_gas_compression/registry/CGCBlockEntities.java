package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import com.velocity1029.create_gas_compression.blocks.engines.EngineBlockEntity;
import com.velocity1029.create_gas_compression.blocks.engines.EngineRenderer;
import com.velocity1029.create_gas_compression.blocks.engines.variants.NaturalGasVariant;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankBlockEntity;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankRenderer;

import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

public class CGCBlockEntities {
    //Kinetics
    public static final BlockEntityEntry<EngineBlockEntity> NATURAL_GAS_ENGINE = REGISTRATE
            .blockEntity("natural_gas_engine", EngineBlockEntity.create(new NaturalGasVariant()))
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(CGCBlocks.NATURAL_GAS_ENGINE)
            .renderer(() -> EngineRenderer::new)
            .register();

    public static final BlockEntityEntry<IronPipeBlockEntity> IRON_PIPE = REGISTRATE
            .blockEntity("iron_pipe", IronPipeBlockEntity::new)
            .validBlocks(CGCBlocks.IRON_PIPE)
            .register();

    public static final BlockEntityEntry<IronTankBlockEntity> IRON_TANK = REGISTRATE
            .blockEntity("iron_tank", IronTankBlockEntity::new)
            .validBlocks(CGCBlocks.IRON_TANK)
            .renderer(() -> IronTankRenderer::new)
            .register();

    public static final BlockEntityEntry<CompressorFrameBlockEntity> COMPRESSOR_FRAME = REGISTRATE
            .blockEntity("compressor_frame", CompressorFrameBlockEntity::new)
            .validBlocks(CGCBlocks.COMPRESSOR_FRAME)
//            .renderer(() -> CompressorFrameBlockRenderer::new)
            .register();

    public static final BlockEntityEntry<CompressorGuideBlockEntity> COMPRESSOR_GUIDE = REGISTRATE
            .blockEntity("compressor_guide", CompressorGuideBlockEntity::new)
            .validBlocks(CGCBlocks.COMPRESSOR_GUIDE)
//            .renderer(() -> CompressorGuideBlockRenderer::new)
            .register();

    public static final BlockEntityEntry<CompressorCylinderBlockEntity> COMPRESSOR_CYLINDER = REGISTRATE
            .blockEntity("compressor_cylinder", CompressorCylinderBlockEntity::new)
            .validBlocks(CGCBlocks.COMPRESSOR_CYLINDER)
//            .renderer(() -> CompressorCylinderBlockRenderer::new)
            .register();

    // Load this class

    public static void register() {
    }
}
