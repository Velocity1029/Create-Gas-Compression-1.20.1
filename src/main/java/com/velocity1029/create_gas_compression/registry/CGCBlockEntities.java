package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.velocity1029.create_gas_compression.blocks.engines.EngineBlockEntity;
import com.velocity1029.create_gas_compression.blocks.engines.EngineRenderer;
import com.velocity1029.create_gas_compression.blocks.engines.variants.NaturalGasVariant;

import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

public class CGCBlockEntities {
    //Kinetics
    public static final BlockEntityEntry<EngineBlockEntity> NATURAL_GAS_ENGINE = REGISTRATE
            .blockEntity("natural_gas_engine", EngineBlockEntity.create(new NaturalGasVariant()))
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(CGCBlocks.NATURAL_GAS_ENGINE)
            .renderer(() -> EngineRenderer::new)
            .register();

    // Load this class

    public static void register() {
    }
}
