package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import com.velocity1029.create_gas_compression.blocks.diffuser.DiffuserBlockEntity;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankBlockEntity;
import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import com.velocity1029.create_gas_compression.registry.CGCTags;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class CompressorCylinderBlockEntity extends PumpBlockEntity  {

    // Fluid Handling
    protected FluidTank tank;

    Couple<MutableBoolean> sidesToUpdate;
    boolean pressureUpdate;

    public BlockPos guidePos;
    public float compressorEfficiency;
    public int initialTicks;
    public static Block guideKey;

    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        tank = new CompressorTank();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new CompressorFluidTransferBehaviour(this));
    }

    public void update(BlockPos sourcePos, float efficiency) {
        guidePos = worldPosition.subtract(sourcePos);
        compressorEfficiency = efficiency;

        guideKey = level.getBlockState(sourcePos)
                .getBlock();
        BlockEntity blockEntity = level.getBlockEntity(sourcePos);
        if (blockEntity instanceof CompressorGuideBlockEntity guideBlockEntity) {
            CompressorFrameBlockEntity frame = guideBlockEntity.getFrame();
            if (frame == null) {
                if (hasSource()) removeSource();
            } else {
                setSource(frame.getBlockPos());
            }
        }
    }

    public void remove(BlockPos sourcePos) {
        if (!isPoweredBy(sourcePos))
            return;

        guidePos = null;
        compressorEfficiency = 0;
        guideKey = null;
        removeSource();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return initialTicks == 0 && (guidePos == null || isPoweredBy(globalPos));
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        BlockPos key = worldPosition.subtract(globalPos);
        return key.equals(guidePos);
    }

    protected boolean isFront(Direction side) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return false;
        Direction front = blockState.getValue(CompressorCylinderBlock.FACING);
        return side == front;
    }

    @Nullable
    protected Direction getFront() {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return null;
        return blockState.getValue(CompressorCylinderBlock.FACING);
    }

    public boolean isSideAccessible(Direction side) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return false;
        return blockState.getValue(CompressorCylinderBlock.FACING)
                .getAxis() == side.getAxis();
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide && !isVirtual())
            return;

        sidesToUpdate.forEachWithContext((update, isFront) -> {
            if (update.isFalse())
                return;
            update.setFalse();
            distributePressureTo(isFront ? getFront() : getFront().getOpposite());
        });
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);

        if (Math.abs(previousSpeed) == Math.abs(getSpeed()))
            return;
        if (level.isClientSide && !isVirtual())
            return;

        updatePressureChange();
    }

    public void updatePressureChange() {
        pressureUpdate = false;
        BlockPos frontPos = worldPosition.relative(getFront());
        BlockPos backPos = worldPosition.relative(getFront().getOpposite());
        FluidPropagator.propagateChangedPipe(level, frontPos, level.getBlockState(frontPos));
        FluidPropagator.propagateChangedPipe(level, backPos, level.getBlockState(backPos));

        FluidTransportBehaviour behaviour = getBehaviour(FluidTransportBehaviour.TYPE);
        if (behaviour != null)
            behaviour.wipePressure();
        sidesToUpdate.forEach(MutableBoolean::setTrue);
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        tank.readFromNBT(compound.getCompound("Tank"));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("Tank", tank.writeToNBT(new CompoundTag()));
    }

    protected void distributePressureTo(Direction side) {
        if (getSpeed() == 0)
            return;

        BlockFace start = new BlockFace(worldPosition, side);
        boolean pull = isPullingOnSide(isFront(side));
        Set<BlockFace> targets = new HashSet<>();
        Map<BlockPos, Integer> diffusedPipes = new HashMap<>();
        Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph = new HashMap<>();

        if (!pull)
            FluidPropagator.resetAffectedFluidNetworks(level, worldPosition, side.getOpposite());

        if (!hasReachedValidEndpoint(level, start, pull)) {

            pipeGraph.computeIfAbsent(worldPosition, $ -> Pair.of(0, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side, pull);
            pipeGraph.computeIfAbsent(start.getConnectedPos(), $ -> Pair.of(1, new IdentityHashMap<>()))
                    .getSecond()
                    .put(side.getOpposite(), !pull);

            List<Pair<Integer, BlockPos>> frontier = new ArrayList<>();
            Set<BlockPos> visited = new HashSet<>();
            int maxDistance = CreateGasCompressionConfig.getServer().compressorCylinderRange.get();
            frontier.add(Pair.of(1, start.getConnectedPos()));

            while (!frontier.isEmpty()) {
                Pair<Integer, BlockPos> entry = frontier.remove(0);
                int distance = entry.getFirst();
                BlockPos currentPos = entry.getSecond();

                if (!level.isLoaded(currentPos))
                    continue;
                if (visited.contains(currentPos))
                    continue;
                visited.add(currentPos);
                BlockState currentState = level.getBlockState(currentPos);
                FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, currentPos);
                if (pipe == null)
                    continue;

                boolean diffusedConnection = false;
                int diffusionRatio = 1;
                if (diffusedPipes.containsKey(currentPos)) {
                    diffusionRatio *= diffusedPipes.get(currentPos);
                    diffusedConnection = true;
                }
                if (level.getBlockEntity(currentPos) instanceof DiffuserBlockEntity) {
                    diffusionRatio *= FluidPropagator.getPipeConnections(currentState, pipe).size() - 1;
                    diffusedConnection = true;
                }
                for (Direction face : FluidPropagator.getPipeConnections(currentState, pipe)) {
                    BlockFace blockFace = new BlockFace(currentPos, face);
                    BlockPos connectedPos = blockFace.getConnectedPos();

                    if (!level.isLoaded(connectedPos))
                        continue;
                    if (blockFace.isEquivalent(start))
                        continue;
                    if (hasReachedValidEndpoint(level, blockFace, pull)) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, connectedPos);
                    if (pipeBehaviour == null)
                        continue;
                    if (pipeBehaviour instanceof CompressorFluidTransferBehaviour)
                        continue;
                    if (visited.contains(connectedPos))
                        continue;
                    if (distance + 1 >= maxDistance) {
                        pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                                .getSecond()
                                .put(face, pull);
                        targets.add(blockFace);
                        continue;
                    }

                    pipeGraph.computeIfAbsent(currentPos, $ -> Pair.of(distance, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face, pull);
                    pipeGraph.computeIfAbsent(connectedPos, $ -> Pair.of(distance + 1, new IdentityHashMap<>()))
                            .getSecond()
                            .put(face.getOpposite(), !pull);
                    frontier.add(Pair.of(distance + 1, connectedPos));
                    if (diffusedConnection) {
                        diffusedPipes.put(connectedPos, diffusionRatio);
                    }
                }
            }
        }

        // DFS
        Map<Integer, Set<BlockFace>> validFaces = new HashMap<>();
        searchForEndpointRecursively(pipeGraph, targets, validFaces,
                new BlockFace(start.getPos(), start.getOppositeFace()), pull);

        float pressure = Math.abs(getSpeed());
        for (Set<BlockFace> set : validFaces.values()) {
            int parallelBranches = Math.max(1, set.size() - 1);
            for (BlockFace face : set) {
                BlockPos pipePos = face.getPos();
                Direction pipeSide = face.getFace();

                if (pipePos.equals(worldPosition))
                    continue;

                boolean inbound = pipeGraph.get(pipePos)
                        .getSecond()
                        .get(pipeSide);
                FluidTransportBehaviour pipeBehaviour = FluidPropagator.getPipe(level, pipePos);
                if (pipeBehaviour == null)
                    continue;
                float correctedPressure = pull? pressure : pressure / 2;
                if (diffusedPipes.containsKey(pipePos)) {
                    correctedPressure *= diffusedPipes.get(pipePos);
                }
                pipeBehaviour.addPressure(pipeSide, inbound, correctedPressure / parallelBranches);
            }
        }

    }

    private boolean hasReachedValidEndpoint(LevelAccessor world, BlockFace blockFace, boolean pull) {
        BlockPos connectedPos = blockFace.getConnectedPos();
        BlockState connectedState = world.getBlockState(connectedPos);
        BlockEntity blockEntity = world.getBlockEntity(connectedPos);
        Direction face = blockFace.getFace();

        // facing a pump
        if (PumpBlock.isPump(connectedState) && connectedState.getValue(PumpBlock.FACING)
                .getAxis() == face.getAxis() && blockEntity instanceof CompressorCylinderBlockEntity cylinderBE) {
            return cylinderBE.isPullingOnSide(cylinderBE.isFront(blockFace.getOppositeFace())) != pull;
        }

        // other pipe, no endpoint
        FluidTransportBehaviour pipe = FluidPropagator.getPipe(world, connectedPos);
        if (pipe != null && pipe.canHaveFlowToward(connectedState, blockFace.getOppositeFace()))
            return false;

        // fluid handler endpoint
        if (blockEntity != null) {
            LazyOptional<IFluidHandler> capability =
                    blockEntity.getCapability(ForgeCapabilities.FLUID_HANDLER, face.getOpposite());
            if (capability.isPresent())
                return true;
        }

        // open endpoint
        return FluidPropagator.isOpenEnd(world, blockFace.getPos(), face);
    }

    public void updatePipesOnSide(Direction side) {
        if (!isSideAccessible(side))
            return;
        updatePipeNetwork(isFront(side));
        getBehaviour(FluidTransportBehaviour.TYPE).wipePressure();
    }

    protected void updatePipeNetwork(boolean front) {
        sidesToUpdate.get(front)
                .setTrue();
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (other instanceof CompressorGuideBlockEntity guideTarget) {
            CompressorCylinderBlockEntity cylinder = guideTarget.getCylinder();
            return cylinder != null && cylinder.equals(this);
        }
        return false;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking,
                getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> tank).cast();
        return super.getCapability(cap, side);
    }

    protected static FluidStack pressurizeFluid(FluidStack fluid) {
        if (fluid.isEmpty() ) return fluid;
        fluid.setAmount(fluid.getAmount() / 2);
        CompoundTag tags = fluid.getOrCreateTag();
        float pressure = tags.contains("Pressure", Tag.TAG_FLOAT) ? tags.getFloat("Pressure") : 1;
        tags.putFloat("Pressure", pressure * 2f);
        tags.putBoolean("Hot", true);
        return fluid;
    }

    class CompressorFluidTransferBehaviour extends PressurizedFluidTransportBehaviour {

        public CompressorFluidTransferBehaviour(SmartBlockEntity be) {
            super(be);
            sidesToUpdate = Couple.create(MutableBoolean::new);
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return pressurizeFluid(superFluid);
        }

        @Override
        public void tick() {
            super.tick();
            if (interfaces == null) return;
            for (Map.Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
                boolean pull = isPullingOnSide(isFront(entry.getKey()));
                Couple<Float> pressure = entry.getValue().getPressure();
                pressure.set(pull, Math.abs(getSpeed()));
                pressure.set(!pull, 0f);
            }
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return isSideAccessible(direction);
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                        Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
            if (attachment == AttachmentTypes.RIM)
                return AttachmentTypes.NONE;
            return attachment;
        }
    }

    private class CompressorTank extends FluidTank {

        public CompressorTank() {
            super(1000, (e) -> e.getFluid().is(CGCTags.CGCFluidTags.GAS.tag) // Is Fluid a gas ?
            && (!e.hasTag() || !e.getTag().getBoolean("Hot"))); // Is Fluid not already hot ?
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (!level.isClientSide) {
                setChanged();
                sendData();
            }
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.isEmpty() && this.isFluidValid(resource)) {
                if (action.simulate()) {
                    if (this.fluid.isEmpty()) {
                        return Math.min(this.capacity * 2, resource.getAmount());
                    } else {
                        return this.fluid.getFluid() != resource.getFluid() ? 0 : Math.min(this.capacity - this.fluid.getAmount(), resource.getAmount());
                    }
                } else if (this.fluid.isEmpty()) {
                    this.fluid = pressurizeFluid(new FluidStack(resource, Math.min(this.capacity * 2, resource.getAmount())));
                    this.onContentsChanged();
                    return Math.min(this.capacity * 2, resource.getAmount());
                } else if (this.fluid.getFluid() != resource.getFluid()) {
                    return 0;
                } else {
                    int filled = this.capacity - this.fluid.getAmount();
                    if (resource.getAmount() < filled * 2) {
                        this.fluid.grow(resource.getAmount() / 2);
                        filled = resource.getAmount();
                    } else {
                        this.fluid.setAmount(this.capacity);
                    }

                    if (filled > 0) {
                        this.onContentsChanged();
                    }

                    return filled;
                }
            } else {
                return 0;
            }
        }
    }
}
