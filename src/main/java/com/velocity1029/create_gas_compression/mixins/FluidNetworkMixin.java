package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.content.fluids.FluidNetwork;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.velocity1029.create_gas_compression.base.FluidTransformer;
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Supplier;

@Mixin(FluidNetwork.class)
public class FluidNetworkMixin {

    @Shadow(remap = false)
    private static int CYCLES_PER_TICK = 16;

    @Shadow(remap = false)
    Level world;
    @Shadow(remap = false)
    BlockFace start;

    @Shadow(remap = false)
    Supplier<LazyOptional<IFluidHandler>> sourceSupplier;
    @Shadow(remap = false)
    LazyOptional<IFluidHandler> source;
    @Shadow(remap = false)
    int transferSpeed;

    @Shadow(remap = false)
    int pauseBeforePropagation;
    @Shadow(remap = false)
    List<BlockFace> queued;
    @Shadow(remap = false)
    Set<Pair<BlockFace, PipeConnection>> frontier;
    @Shadow(remap = false)
    Set<BlockPos> visited;
    @Shadow(remap = false)
    FluidStack fluid;
    @Shadow(remap = false)
    List<Pair<BlockFace, LazyOptional<IFluidHandler>>> targets;
    @Shadow(remap = false)
    Map<BlockPos, WeakReference<FluidTransportBehaviour>> cache;
    @Unique
    Map<BlockPos, ArrayList<FluidTransformer>> $_fluidTransformers = new HashMap<>();

    /**
     * @author Velocity1029/
     * @reason Allow fluid tags to be altered along a flow and account for fluid transformers along a flow.
     */
    @Overwrite(remap = false)
    public void tick() {
        if (pauseBeforePropagation > 0) {
            pauseBeforePropagation--;
            return;
        }

        for (int cycle = 0; cycle < CYCLES_PER_TICK; cycle++) {
            boolean shouldContinue = false;
            for (Iterator<BlockFace> iterator = queued.iterator(); iterator.hasNext();) {
                BlockFace blockFace = iterator.next();
                if (!isPresent(blockFace))
                    continue;
                PipeConnection pipeConnection = get(blockFace);
                if (pipeConnection != null) {
                    if (blockFace.equals(start))
                        transferSpeed = (int) Math.max(1, pipeConnection.getPressure().get(true) / 2f);
                    frontier.add(Pair.of(blockFace, pipeConnection));
                    ArrayList<FluidTransformer> transformers = $_fluidTransformers.get(blockFace.getPos());
                    ArrayList<FluidTransformer> adjacentTransformers = $_fluidTransformers.get(blockFace.getConnectedPos());
                    ArrayList<FluidTransformer> reversedList = new ArrayList<>();
                    if (transformers != null && adjacentTransformers != null) {
                        reversedList.addAll(adjacentTransformers);
                        reversedList.addAll(transformers);
                        $_fluidTransformers.put(blockFace.getPos(), reversedList);
                    }
                }
                iterator.remove();
            }

//			drawDebugOutlines();

            for (Iterator<Pair<BlockFace, PipeConnection>> iterator = frontier.iterator(); iterator.hasNext();) {
                Pair<BlockFace, PipeConnection> pair = iterator.next();
                BlockFace blockFace = pair.getFirst();
                PipeConnection pipeConnection = pair.getSecond();

                if (!pipeConnection.hasFlow())
                    continue;

                PipeConnection.Flow flow = ((PipeConnectionAccessor) pipeConnection).getFlow().get();
                if (!fluid.isEmpty() && flow.fluid.getFluid() != fluid.getFluid()) {//!flow.fluid.isFluidEqual(fluid)) {
                    iterator.remove();
                    continue;
                }
                if (!flow.inbound) {
                    if (pipeConnection.comparePressure() >= 0)
                        iterator.remove();
                    continue;
                }
                if (!flow.complete)
                    continue;

                if (fluid.isEmpty())
                    fluid = flow.fluid;

                boolean canRemove = true;
                for (Direction side : Iterate.directions) {
                    if (side == blockFace.getFace())
                        continue;
                    BlockFace adjacentLocation = new BlockFace(blockFace.getPos(), side);
                    PipeConnection adjacent = get(adjacentLocation);
                    if (adjacent == null)
                        continue;
                    if (!adjacent.hasFlow()) {
                        // Branch could potentially still appear
                        if (adjacent.hasPressure() && adjacent.getPressure().getSecond() > 0)
                            canRemove = false;
                        continue;
                    }
                    PipeConnection.Flow outFlow = ((PipeConnectionAccessor) adjacent).getFlow().get();
                    if (outFlow.inbound) {
                        if (adjacent.comparePressure() > 0)
                            canRemove = false;
                        continue;
                    }
                    if (!outFlow.complete) {
                        canRemove = false;
                        continue;
                    }

                    // Give pipe end a chance to init connections
                    if (!((PipeConnectionAccessor) adjacent).getSource().isPresent() && !adjacent.determineSource(world, blockFace.getPos())) {
                        canRemove = false;
                        continue;
                    }

                    if (((PipeConnectionAccessor) adjacent).getSource().isPresent() && ((PipeConnectionAccessor) adjacent).getSource().get()
                            .isEndpoint()) {
                        targets.add(Pair.of(adjacentLocation, ((PipeConnectionAccessor) adjacent).getSource().get()
                                .provideHandler()));
                        continue;
                    }

                    if (visited.add(adjacentLocation.getConnectedPos())) {
                        queued.add(adjacentLocation.getOpposite());
                        shouldContinue = true;
                    }
                }
                if (canRemove)
                    iterator.remove();
            }
            if (!shouldContinue)
                break;
        }

//		drawDebugOutlines();

        if (!source.isPresent())
            source = sourceSupplier.get();
        if (!source.isPresent())
            return;

        keepPortableFluidInterfaceEngaged();

        if (targets.isEmpty())
            return;
        for (Pair<BlockFace, LazyOptional<IFluidHandler>> pair : targets) {
            if (pair.getSecond()
                    .isPresent() && world.getGameTime() % 40 != 0)
                continue;
            PipeConnection pipeConnection = get(pair.getFirst());
            if (pipeConnection == null)
                continue;
            ((PipeConnectionAccessor) pipeConnection).getSource().ifPresent(fs -> {
                if (fs.isEndpoint())
                    pair.setSecond(fs.provideHandler());
            });
        }

        int flowSpeed = transferSpeed;
        Map<IFluidHandler, Integer> accumulatedFill = new IdentityHashMap<>();

        for (boolean simulate : Iterate.trueAndFalse) {
            IFluidHandler.FluidAction action = simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE;

            IFluidHandler handler = source.orElse(null);
            if (handler == null)
                return;

            FluidStack transfer = FluidStack.EMPTY;
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack contained = handler.getFluidInTank(i);
                if (contained.isEmpty())
                    continue;
                if (!contained.isFluidEqual(fluid))
                    continue;
                FluidStack toExtract = FluidHelper.copyStackWithAmount(contained, flowSpeed);
                transfer = handler.drain(toExtract, action);
            }

            if (transfer.isEmpty()) {
                FluidStack genericExtract = handler.drain(flowSpeed, action);
                if (!genericExtract.isEmpty() && genericExtract.isFluidEqual(fluid))
                    transfer = genericExtract;
            }

            if (transfer.isEmpty())
                return;
            if (simulate)
                flowSpeed = transfer.getAmount();

            List<Pair<BlockFace, LazyOptional<IFluidHandler>>> availableOutputs = new ArrayList<>(targets);

            while (!availableOutputs.isEmpty() && transfer.getAmount() > 0) {
                int dividedTransfer = transfer.getAmount() / availableOutputs.size();
                int remainder = transfer.getAmount() % availableOutputs.size();

                for (Iterator<Pair<BlockFace, LazyOptional<IFluidHandler>>> iterator =
                     availableOutputs.iterator(); iterator.hasNext();) {
                    Pair<BlockFace, LazyOptional<IFluidHandler>> pair = iterator.next();
                    int toDrain = dividedTransfer;

                    if (remainder > 0) {
                        toDrain++;
                        remainder--;
                    }

                    if (transfer.isEmpty())
                        break;
                    IFluidHandler targetHandler = pair.getSecond()
                            .orElse(null);
                    if (targetHandler == null) {
                        iterator.remove();
                        continue;
                    }

                    ArrayList<FluidTransformer> outputTransformers = $_fluidTransformers.get(pair.getFirst().getPos());
                    FluidStack transformerFluid = new FluidStack(transfer, toDrain);

                    // Compression loss correction variable
                    int compressionLoss = 0;
                    int toFill = toDrain;
                    if (outputTransformers != null) {
                        for (FluidTransformer fluidTransformer : outputTransformers) {
                            int preTransformedAmount = transformerFluid.getAmount();

                            fluidTransformer.transformFluid(transformerFluid);

                            if (fluidTransformer instanceof CompressorCylinderBlockEntity.CompressorFluidTransferBehaviour compressionTransformer) {
                                int compressionRemainder = preTransformedAmount % 2;
                                compressionLoss += compressionRemainder * ((int) transformerFluid.getTag().getFloat("Pressure") / 2);
                            }
                        }
                        toFill = transformerFluid.getAmount();
                    }

                    if (transformerFluid.isEmpty()) {
                        iterator.remove();
                        break;
                    }

                    int simulatedTransfer = toFill;
                    if (simulate)
                        simulatedTransfer += accumulatedFill.getOrDefault(targetHandler, 0);

                    FluidStack divided = transformerFluid.copy();
                    divided.setAmount(simulatedTransfer);

                    int fill = targetHandler.fill(divided, action);

                    if (simulate) {
                        accumulatedFill.put(targetHandler, Integer.valueOf(fill));
                        fill -= simulatedTransfer - toFill;
                    }

                    transfer.setAmount(transfer.getAmount() + compressionLoss);
                    if (fill != 0)
                        transfer.setAmount(transfer.getAmount() - ((toDrain * transformerFluid.getAmount()) / fill));
                    if (fill < simulatedTransfer)
                        iterator.remove();
                }

            }

            flowSpeed -= transfer.getAmount();
            transfer = FluidStack.EMPTY;
        }
    }


    /**
     * @author Velocity1029/
     * @reason Used to record fluidTransformers for use in tick
     */
    @Overwrite(remap = false)
    @Nullable
    private FluidTransportBehaviour getFluidTransfer(BlockPos pos) {
        WeakReference<FluidTransportBehaviour> weakReference = cache.get(pos);
        FluidTransportBehaviour behaviour = weakReference != null ? weakReference.get() : null;
        if (behaviour != null && behaviour.blockEntity.isRemoved())
            behaviour = null;
        if (behaviour == null) {
            behaviour = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
            if (behaviour != null) {
                cache.put(pos, new WeakReference<>(behaviour));
                ArrayList<FluidTransformer> transformers = new ArrayList<>();
                if (behaviour instanceof FluidTransformer fluidTransformer) {
                    transformers.add(fluidTransformer);
                }
                $_fluidTransformers.put(pos, transformers);
            }
        }
        return behaviour;
    }

    @Shadow()
    private boolean isPresent(BlockFace location) {
        throw new IllegalStateException("Mixin failed to shadow isPresent()");
    }

    @Shadow()
    @Nullable
    private PipeConnection get(BlockFace location) {
        throw new IllegalStateException("Mixin failed to shadow get()");
    }

    @Shadow()
    private void keepPortableFluidInterfaceEngaged() {
        throw new IllegalStateException("Mixin failed to shadow keepPortableFluidInterfaceEngaged()");
    }
}
