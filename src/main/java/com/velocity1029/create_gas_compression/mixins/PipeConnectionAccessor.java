package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.PipeConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Optional;

@Mixin(PipeConnection.class)
public interface PipeConnectionAccessor {
    @Accessor("flow")
    public void setFlow(Optional<PipeConnection.Flow> flow);

    @Accessor
    Optional<PipeConnection.Flow> getFlow();

    @Accessor("source")
    public void setSource(Optional<FlowSource> source);

    @Accessor
    Optional<FlowSource> getSource();
}
