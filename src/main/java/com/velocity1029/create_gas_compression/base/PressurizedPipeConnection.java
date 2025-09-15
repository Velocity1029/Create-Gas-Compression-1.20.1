package com.velocity1029.create_gas_compression.base;

import com.simibubi.create.content.fluids.*;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

// Just trust me bro...
public class PressurizedPipeConnection extends PipeConnection{

    Method tryStartingNewFlow;

    Field fPressure;
    Field fSource;
    Field fFlow;
    Field fNetwork;

    @Override
    public Couple<Float> getPressure() {
        try {
            return (Couple<Float>) fPressure.get(this); //IllegalAccessException;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPressure(Couple<Float> pressure) {
        try {
            this.fPressure.set(this, pressure);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<FlowSource> getSource() {
        try {
            return (Optional<FlowSource>) fSource.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setSource(Optional<FlowSource> source) {
        try {
            this.fSource.set(this, source);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Flow> getFlow() {
        try {
            return (Optional<Flow>) fFlow.get(this); //IllegalAccessException;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setFlow(Optional<Flow> flow) {
        try {
            this.fFlow.set(this, flow);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<PressurizedFluidNetwork> getNetwork() {
        try {
            return (Optional<PressurizedFluidNetwork>) fNetwork.get(this); //IllegalAccessException
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void setNetwork(Optional<PressurizedFluidNetwork> network) {
        try {
            this.fNetwork.set(this, network);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
    {
        try {
            tryStartingNewFlow = getClass().getSuperclass().getDeclaredMethod("tryStartingNewFlow", boolean.class, FluidStack.class);
            fPressure = getClass().getSuperclass().getDeclaredField("pressure"); //NoSuchFieldException
            fSource = getClass().getSuperclass().getDeclaredField("source"); //NoSuchFieldException
            fFlow = getClass().getSuperclass().getDeclaredField("flow"); //NoSuchFieldException
            fNetwork = getClass().getSuperclass().getDeclaredField("network"); //NoSuchFieldException
            tryStartingNewFlow.setAccessible(true);
            fPressure.setAccessible(true);
            fSource.setAccessible(true);
            fFlow.setAccessible(true);
            fNetwork.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public PressurizedPipeConnection(Direction side) {
        super(side);
    }

    @Override
    public boolean manageFlows(Level world, BlockPos pos, FluidStack internalFluid,
                               Predicate<FluidStack> extractionPredicate) {
//        super.manageFlows(world, pos, internalFluid, extractionPredicate);

        // Only keep network if still valid
        Optional<PressurizedFluidNetwork> retainedNetwork = getNetwork();
        setNetwork(Optional.empty());

        // chunk border
        if (!getSource().isPresent() && !determineSource(world, pos))
            return false;
        FlowSource flowSource = getSource().get();

        if (!hasFlow()) {
            if (!hasPressure())
                return false;

            // Try starting a new flow
            boolean prioritizeInbound = comparePressure() < 0;
            for (boolean trueFalse : Iterate.trueAndFalse) {
                boolean inbound = prioritizeInbound == trueFalse;
                if (getPressure().get(inbound) == 0)
                    continue;
                try {
                    Object result = tryStartingNewFlow.invoke(this, inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid);
                    if ((result instanceof Boolean) && (boolean) result)
                        return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }

        // Manage existing flow
        Flow flow = this.getFlow().get();
        FluidStack provided = flow.inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid;
        if (!hasPressure() || provided.isEmpty() || !provided.isFluidEqual(flow.fluid)) {
            this.setFlow(Optional.empty());
            return true;
        }

        // Overwrite existing flow
        if (flow.inbound != comparePressure() < 0) {
            boolean inbound = !flow.inbound;
            if (inbound && !provided.isEmpty() || !inbound && !internalFluid.isEmpty()) {
                FluidPropagator.resetAffectedFluidNetworks(world, pos, side);
                try {
                    Object result = tryStartingNewFlow.invoke(this, inbound, inbound ? flowSource.provideFluid(extractionPredicate) : internalFluid);
                    if ((result instanceof Boolean) && (boolean) result)
                        return true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
        }

        flowSource.whileFlowPresent(world, flow.inbound);

        if (!flowSource.isEndpoint())
            return false;
        if (!flow.inbound)
            return false;

        // Layer III
        setNetwork(retainedNetwork);
        if (!hasNetwork())
            setNetwork(Optional.of(new PressurizedFluidNetwork(world, new BlockFace(pos, side), flowSource::provideHandler)));
        getNetwork().get()
                .tick();

        return false;
    }

    public void resetNetwork() {
        getNetwork().ifPresent(PressurizedFluidNetwork::reset);
    }

}
