package com.velocity1029.create_gas_compression.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.actors.psi.PortableFluidInterfaceBlockEntity.InterfaceFluidHandler;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.FluidHelper;

import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlockEntity;
import net.createmod.catnip.math.BlockFace;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;

public class PressurizedFluidNetwork {

    private static int CYCLES_PER_TICK = 16;

    Level world;
    BlockFace start;

    Supplier<LazyOptional<IFluidHandler>> sourceSupplier;
    LazyOptional<IFluidHandler> source;
    int transferSpeed;

    int pauseBeforePropagation;
    List<BlockFace> queued;
    Set<Pair<BlockFace, PressurizedPipeConnection>> frontier;
    Set<BlockPos> visited;
    FluidStack fluid;
    List<Pair<BlockFace, LazyOptional<IFluidHandler>>> targets;
    Map<BlockPos, WeakReference<FluidTransportBehaviour>> cache;
    Map<BlockPos, ArrayList<FluidTransformer>> fluidTransformers;

    public PressurizedFluidNetwork(Level world, BlockFace location, Supplier<LazyOptional<IFluidHandler>> sourceSupplier) {
        this.world = world;
        this.start = location;
        this.sourceSupplier = sourceSupplier;
        this.source = LazyOptional.empty();
        this.fluid = FluidStack.EMPTY;
        this.frontier = new HashSet<>();
        this.visited = new HashSet<>();
        this.targets = new ArrayList<>();
        this.cache = new HashMap<>();
        this.fluidTransformers = new HashMap<>();
        this.queued = new ArrayList<>();
        reset();
    }

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
                PressurizedPipeConnection pipeConnection = get(blockFace);
                if (pipeConnection != null) {
                    if (blockFace.equals(start))
                        transferSpeed = (int) Math.max(1, pipeConnection.getPressure().get(true) / 2f);
                    frontier.add(Pair.of(blockFace, pipeConnection));
                    ArrayList<FluidTransformer> transformers = fluidTransformers.get(blockFace.getPos());
                    ArrayList<FluidTransformer> adjacentTransformers = fluidTransformers.get(blockFace.getConnectedPos());
                    ArrayList<FluidTransformer> reversedList = new ArrayList<>();
                    if (transformers != null && adjacentTransformers != null)
                        reversedList.addAll(adjacentTransformers);
                        reversedList.addAll(transformers);
                        fluidTransformers.put(blockFace.getPos(), reversedList);
                }
                iterator.remove();
            }

//			drawDebugOutlines();

            for (Iterator<Pair<BlockFace, PressurizedPipeConnection>> iterator = frontier.iterator(); iterator.hasNext();) {
                Pair<BlockFace, PressurizedPipeConnection> pair = iterator.next();
                BlockFace blockFace = pair.getFirst();
                PressurizedPipeConnection pipeConnection = pair.getSecond();

                if (!pipeConnection.hasFlow())
                    continue;

                PressurizedPipeConnection.Flow flow = pipeConnection.getFlow().get();
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
                    PressurizedPipeConnection adjacent = get(adjacentLocation);
                    if (adjacent == null)
                        continue;
                    if (!adjacent.hasFlow()) {
                        // Branch could potentially still appear
                        if (adjacent.hasPressure() && adjacent.getPressure().getSecond() > 0)
                            canRemove = false;
                        continue;
                    }
                    PressurizedPipeConnection.Flow outFlow = adjacent.getFlow().get();
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
                    if (!adjacent.getSource().isPresent() && !adjacent.determineSource(world, blockFace.getPos())) {
                        canRemove = false;
                        continue;
                    }

                    if (adjacent.getSource().isPresent() && adjacent.getSource().get()
                            .isEndpoint()) {
                        targets.add(Pair.of(adjacentLocation, adjacent.getSource().get()
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
            PressurizedPipeConnection pipeConnection = get(pair.getFirst());
            if (pipeConnection == null)
                continue;
            pipeConnection.getSource().ifPresent(fs -> {
                if (fs.isEndpoint())
                    pair.setSecond(fs.provideHandler());
            });
        }

        int flowSpeed = transferSpeed;
        Map<IFluidHandler, Integer> accumulatedFill = new IdentityHashMap<>();

        for (boolean simulate : Iterate.trueAndFalse) {
            FluidAction action = simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE;

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
                    int toTransfer = dividedTransfer;

                    if (remainder > 0) {
                        toTransfer++;
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

                    ArrayList<FluidTransformer> outputTransformers = fluidTransformers.get(pair.getFirst().getPos());
                    FluidStack transformerFluid = new FluidStack(transfer, toTransfer);

                    // Compression loss correction variable
                    int compressionLoss = 0;
                    if (outputTransformers != null) {
                        for (FluidTransformer fluidTransformer : outputTransformers) {
                            int preTransformedAmount = transformerFluid.getAmount();

                            fluidTransformer.transformFluid(transformerFluid);

                            if (fluidTransformer instanceof CompressorCylinderBlockEntity.CompressorFluidTransferBehaviour compressionTransformer) {
                                int compressionRemainder = preTransformedAmount % 2;
                                compressionLoss += compressionRemainder * ((int) transformerFluid.getTag().getFloat("Pressure") / 2);
                            }
                        }
                        toTransfer = transformerFluid.getAmount();
                    }

                    if (transformerFluid.isEmpty()) {
                        iterator.remove();
                        break;
                    }

                    int simulatedTransfer = toTransfer;
                    if (simulate)
                        simulatedTransfer += accumulatedFill.getOrDefault(targetHandler, 0);

                    FluidStack divided = transformerFluid.copy();
                    divided.setAmount(simulatedTransfer);

                    int fill = targetHandler.fill(divided, action);

                    if (simulate) {
                        accumulatedFill.put(targetHandler, Integer.valueOf(fill));
                        fill -= simulatedTransfer - toTransfer;
                    }

                    transfer.setAmount(transfer.getAmount() + compressionLoss);
                    if (fill != 0)
                        transfer.setAmount(transfer.getAmount() - ((dividedTransfer * transformerFluid.getAmount()) / fill));
                    if (fill < simulatedTransfer)
                        iterator.remove();
                }

            }

            flowSpeed -= transfer.getAmount();
            transfer = FluidStack.EMPTY;
        }
    }

//	private void drawDebugOutlines() {
//		FluidPropagator.showBlockFace(start)
//			.lineWidth(1 / 8f)
//			.colored(0xff0000);
//		for (Pair<BlockFace, LazyOptional<IFluidHandler>> pair : targets)
//			FluidPropagator.showBlockFace(pair.getFirst())
//				.lineWidth(1 / 8f)
//				.colored(0x00ff00);
//		for (Pair<BlockFace, PressurizedPipeConnection> pair : frontier)
//			FluidPropagator.showBlockFace(pair.getFirst())
//				.lineWidth(1 / 4f)
//				.colored(0xfaaa33);
//	}

    private void keepPortableFluidInterfaceEngaged() {
        IFluidHandler handler = source.orElse(null);
        if (!(handler instanceof InterfaceFluidHandler))
            return;
        if (frontier.isEmpty())
            return;
        ((InterfaceFluidHandler) handler).keepAlive();
    }

    public void reset() {
        frontier.clear();
        visited.clear();
        targets.clear();
        queued.clear();
        fluid = FluidStack.EMPTY;
        queued.add(start);
        pauseBeforePropagation = 2;
    }

    @Nullable
    private PressurizedPipeConnection get(BlockFace location) {
        BlockPos pos = location.getPos();
        FluidTransportBehaviour fluidTransfer = getFluidTransfer(pos);
        if (fluidTransfer == null)
            return null;
        PipeConnection connection =  fluidTransfer.getConnection(location.getFace());
        return (connection instanceof PressurizedPipeConnection) ? (PressurizedPipeConnection) connection : null;
    }

    private boolean isPresent(BlockFace location) {
        return world.isLoaded(location.getPos());
    }

    @Nullable
    private FluidTransportBehaviour getFluidTransfer(BlockPos pos) {
        WeakReference<FluidTransportBehaviour> weakReference = cache.get(pos);
        FluidTransportBehaviour behaviour = weakReference != null ? weakReference.get() : null;
        if (behaviour != null && behaviour.blockEntity.isRemoved())
            behaviour = null;
        if (behaviour == null) {
            behaviour = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
            if (behaviour != null)
                cache.put(pos, new WeakReference<>(behaviour));
                ArrayList<FluidTransformer> transformers = new ArrayList<>();
                if (behaviour instanceof FluidTransformer fluidTransformer) {
                    transformers.add(fluidTransformer);
                }
                fluidTransformers.put(pos, transformers);
        }
        return behaviour;
    }

}

