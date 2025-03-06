package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.velocity1029.create_gas_compression.CreateGasCompression;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Direction;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class CGCPartialModels {
    public static final PartialModel NATURAL_GAS_COMPRESSOR = block("andesite_chunk_loader/core_active"),
            STATION_ATTACHMENT = block("station_attachment"),
            PIPE_CASING = block("iron_pipe/casing");

    private static PartialModel block(String path) {
        return PartialModel.of(CreateGasCompression.asResource("block/" + path));
    }

    public static final Map<FluidTransportBehaviour.AttachmentTypes.ComponentPartials, Map<Direction, PartialModel>> IRON_PIPE_ATTACHMENTS = new EnumMap<>(FluidTransportBehaviour.AttachmentTypes.ComponentPartials.class);

    static {
        for (FluidTransportBehaviour.AttachmentTypes.ComponentPartials type : FluidTransportBehaviour.AttachmentTypes.ComponentPartials
                .values()) {
            Map<Direction, PartialModel> map = new HashMap<>();
            for (Direction d : Iterate.directions) {
                String asId = Lang.asId(type.name());
                map.put(d, block("iron_pipe/" + asId + "/" + Lang.asId(d.getSerializedName())));
            }
            IRON_PIPE_ATTACHMENTS.put(type, map);
        }
    }

    public static void register() {

    }
}
