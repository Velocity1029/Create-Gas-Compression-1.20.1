package com.velocity1029.create_gas_compression.registry;

import com.velocity1029.create_gas_compression.CreateGasCompression;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public class CGCPartialModels {
    public static final PartialModel NATURAL_GAS_COMPRESSOR = block("andesite_chunk_loader/core_active");
    public static final PartialModel STATION_ATTACHMENT = block("station_attachment");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateGasCompression.asResource("block/" + path));
    }

    public static void register() {

    }
}
