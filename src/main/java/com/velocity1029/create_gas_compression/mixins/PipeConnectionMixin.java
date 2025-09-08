package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.content.fluids.*;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.velocity1029.create_gas_compression.base.PressurizedFluidDistribution;
import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.function.Predicate;

@Mixin(PipeConnection.class)
public class PipeConnectionMixin {

    @Shadow(remap = false)
    public Direction side;

    // Layer I
    @Shadow(remap = false)
    Couple<Float> pressure; // [inbound, outward]
    @Shadow(remap = false)
    Optional<FlowSource> source;
    @Shadow(remap = false)
    Optional<FlowSource> previousSource;

    // Layer II
    @Shadow(remap = false)
    Optional<PipeConnection.Flow> flow;
    @Shadow(remap = false)
    boolean particleSplashNextTick;

    // Layer III
    @Shadow(remap = false)
    Optional<FluidNetwork> network; // not serialized
    
    /**
     * @author Velocity1029/DrMangoTea
     * @reason Allow for flows to be driven by containers containing pressurized fluids, without the need for pumps to draw them out.
     */
    @Overwrite(remap = false)
    public boolean manageFlows(Level world, BlockPos pos, FluidStack internalFluid,
                               Predicate<FluidStack> extractionPredicate) {

        PipeConnection self = (PipeConnection) (Object) this; 
        
        // Only keep network if still valid
        Optional<FluidNetwork> retainedNetwork = network;
        network = Optional.empty();

        // chunk border
        if (!source.isPresent() && !determineSource(world, pos))
            return false;
        FlowSource flowSource = source.get();

        if (!hasFlow()) {
            if (!hasPressure()) {
                FluidStack sourceFluid = flowSource.provideFluid(extractionPredicate);
                CompoundTag fluidTags = sourceFluid.getTag();
                if (fluidTags != null && fluidTags.contains("Pressure", Tag.TAG_FLOAT) && fluidTags.getFloat("Pressure") > 1) {
                    float maxDistance = CreateGasCompressionConfig.getServer().pressurizedFluidRange.get();
                    float maxPressure = (float) Math.pow(2, CreateGasCompressionConfig.getServer().maximumPressureStages.get());
                    Float fluidInducedPressure = fluidTags.getFloat("Pressure") / maxPressure * maxDistance;
                    PressurizedFluidDistribution.distributePressureTo(world, pos.relative(side), side.getOpposite(), fluidInducedPressure);
                }
                else
                    return false;
            }

            // Try starting a new flow
            boolean prioritizeInbound = comparePressure() < 0;
            for (boolean trueFalse : Iterate.trueAndFalse) {
                boolean inbound = prioritizeInbound == trueFalse;
                if (pressure.get(inbound) == 0)
                    continue;
                if (tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid))
                    return true;
            }
            return false;
        }

        // Manage existing flow
        PipeConnection.Flow flow = this.flow.get();
        FluidStack provided = flow.inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid;
        if (!hasPressure() || provided.isEmpty() || !provided.isFluidEqual(flow.fluid)) {
            this.flow = Optional.empty();
            return true;
        }

        // Overwrite existing flow
        if (flow.inbound != comparePressure() < 0) {
            boolean inbound = !flow.inbound;
            if (inbound && !provided.isEmpty() || !inbound && !internalFluid.isEmpty()) {
                FluidPropagator.resetAffectedFluidNetworks(world, pos, side);
                tryStartingNewFlow(inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid);
                return true;
            }
        }

        flowSource.whileFlowPresent(world, flow.inbound);

        if (!flowSource.isEndpoint())
            return false;
        if (!flow.inbound)
            return false;

        // Layer III
        network = retainedNetwork;
        if (!hasNetwork())
            network = Optional.of(new FluidNetwork(world, new BlockFace(pos, side), flowSource::provideHandler));
        network.get()
                .tick();

        return false;
    }

    @Shadow()
    public boolean hasFlow() {
        throw new IllegalStateException("Mixin failed to shadow hasFlow()");
    }

    @Shadow()
    public boolean hasPressure() {
        throw new IllegalStateException("Mixin failed to shadow hasPressure()");
    }

    @Shadow()
    public float comparePressure() {
        throw new IllegalStateException("Mixin failed to shadow comparePressure()");
    }

    @Shadow()
    private boolean tryStartingNewFlow(boolean inbound, FluidStack fluidStack) {
        throw new IllegalStateException("Mixin failed to shadow tryStartingNewFlow()");
    }

    @Shadow()
    public boolean hasNetwork() {
        throw new IllegalStateException("Mixin failed to shadow hasNetwork()");
    }

    @Shadow()
    public boolean determineSource(Level world, BlockPos pos) {
        throw new IllegalStateException("Mixin failed to shadow determineSource()");
    }
}
