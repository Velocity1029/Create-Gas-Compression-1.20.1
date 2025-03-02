package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.OrientedRotatingVisual;
import com.simibubi.create.content.kinetics.motor.CreativeMotorRenderer;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.velocity1029.create_gas_compression.blocks.engines.natural_gas.NaturalGasEngineBlockEntity;
import com.velocity1029.create_gas_compression.blocks.engines.natural_gas.NaturalGasEngineRenderer;
import com.velocity1029.create_gas_compression.blocks.engines.natural_gas.NaturalGasEngineGenerator;

import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

public class BlockEntities {
    //Kinetics
    public static final BlockEntityEntry<NaturalGasEngineBlockEntity> NATURAL_GAS_ENGINE = REGISTRATE
            .blockEntity("natural_gas_engine", NaturalGasEngineBlockEntity::new)
            .visual(() -> OrientedRotatingVisual.of(AllPartialModels.SHAFT_HALF), false)
            .validBlocks(Blocks.NATURAL_GAS_ENGINE)
            .renderer(() -> NaturalGasEngineRenderer::new)
            .register();

    // Load this class

    public static void register() {
    }
}
