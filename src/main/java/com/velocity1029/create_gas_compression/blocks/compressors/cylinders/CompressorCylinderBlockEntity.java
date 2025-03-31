package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineValueBox;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
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
//    protected LazyOptional<IFluidHandler> fluidCapability;
    protected FluidTank tankInput;
    protected FluidTank tankOutput;

    Couple<MutableBoolean> sidesToUpdate;
    boolean pressureUpdate;

    // Backcompat- flips any pump blockstate that loads with reversed=true
    boolean scheduleFlip;

    public BlockPos guidePos;
    public float compressorEfficiency;
//    public int movementDirection;
    public int initialTicks;
    public static Block guideKey;

    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        createFluidTanks();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        behaviours.add(new CompressorFluidTransferBehaviour(this));
    }

    protected void createFluidTanks() {
        tankInput = new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChangedInput);
        tankOutput = new SmartFluidTank(getCapacityMultiplier(), (b) -> {});
    }

    protected void onFluidStackChangedInput(FluidStack newFluidStack) {
        if (newFluidStack != null && !newFluidStack.isEmpty()) {
            int fluidAmount = newFluidStack.getAmount();
            FluidStack outputFluid = pressurizeFluid(newFluidStack);
            int remainingAmount = fluidAmount - (tankOutput.fill(outputFluid, IFluidHandler.FluidAction.EXECUTE) * 2);
            newFluidStack.setAmount(remainingAmount);
        }
    }

    protected FluidStack pressurizeFluid(FluidStack fluid) {
        FluidStack outputFluid = fluid.copy();
        outputFluid.setAmount(outputFluid.getAmount() / 2);
        CompoundTag outputTags = outputFluid.getOrCreateTag();
        float pressure = outputTags.contains("Pressure", Tag.TAG_FLOAT) ? outputTags.getFloat("Pressure") : 1;
        outputTags.putFloat("Pressure", pressure * 2f);
        outputTags.putBoolean("Hot", true);
        return outputFluid;
    }

    public void update(BlockPos sourcePos, float efficiency) {
        BlockPos key = worldPosition.subtract(sourcePos);
        guidePos = key;
        float prev = compressorEfficiency;
        compressorEfficiency = efficiency;
//        int prevDirection = this.movementDirection;
//        if (Mth.equal(efficiency, prev)) //&& prevDirection == direction)
//            return;

//        setSpeed(efficiency);

        guideKey = level.getBlockState(sourcePos)
                .getBlock();
        BlockEntity blockEntity = level.getBlockEntity(sourcePos);
        if (blockEntity instanceof CompressorGuideBlockEntity guideBlockEntity) {
//            BlockEntity controller = level.getBlockEntity(guideBlockEntity.getFrame().getController());
            CompressorFrameBlockEntity frame = guideBlockEntity.getFrame();
            if (frame == null) {
                if (hasSource()) removeSource();
            } else {
                setSource(frame.getBlockPos());
            }
//            if (controller instanceof CompressorFrameBlockEntity frameBlockEntity) {
//                frameBlockEntity.getOrCreateNetwork().add(this);
//
//            }
        }
//        this.movementDirection = direction; unnecessary
//        // TODO make pump equivalent of updateGeneratedRotation in PoweredShaftBlockEntity
//        updatepumpfluidspeeddirectionstuffandthings();
//
    }

    public void remove(BlockPos sourcePos) {
        if (!isPoweredBy(sourcePos))
            return;

        guidePos = null;
        compressorEfficiency = 0;
//        movementDirection = 0; unnecessary
        guideKey = null;
        removeSource();
//        updatepumpfluidspeeddirectionstuffandthings();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return initialTicks == 0 && (guidePos == null || isPoweredBy(globalPos));
    }

    public float getImpact() {
        float impact = (float) BlockStressValues.getImpact(getStressConfigKey());
        return impact;
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
        boolean isFront = side == front;
        return isFront;
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

        if (scheduleFlip) {
            level.setBlockAndUpdate(worldPosition,
                    getBlockState().setValue(PumpBlock.FACING, getBlockState().getValue(PumpBlock.FACING)
                            .getOpposite()));
            scheduleFlip = false;
        }

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
//        if (speed != 0)
//            award(AllAdvancements.PUMP);
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
        tankInput.readFromNBT(compound.getCompound("TankInput"));
        tankOutput.readFromNBT(compound.getCompound("TankOutput"));
    }

    @Override
    protected void write(CompoundTag compound, boolean clientPacket) {
        super.write(compound, clientPacket);
        compound.put("TankInput", tankInput.writeToNBT(new CompoundTag()));
        compound.put("TankOutput", tankOutput.writeToNBT(new CompoundTag()));
    }

    protected void distributePressureTo(Direction side) {
        if (getSpeed() == 0)
            return;

        BlockFace start = new BlockFace(worldPosition, side);
        boolean pull = isPullingOnSide(isFront(side));
        Set<BlockFace> targets = new HashSet<>();
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
            int maxDistance = FluidPropagator.getPumpRange();   //TODO higher max range for compressors
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

                pipeBehaviour.addPressure(pipeSide, inbound, pressure / parallelBranches);
            }
        }

    }

    protected boolean searchForEndpointRecursively(Map<BlockPos, Pair<Integer, Map<Direction, Boolean>>> pipeGraph,
                                                   Set<BlockFace> targets, Map<Integer, Set<BlockFace>> validFaces, BlockFace currentFace, boolean pull) {
        BlockPos currentPos = currentFace.getPos();
        if (!pipeGraph.containsKey(currentPos))
            return false;
        Pair<Integer, Map<Direction, Boolean>> pair = pipeGraph.get(currentPos);
        int distance = pair.getFirst();

        boolean atLeastOneBranchSuccessful = false;
        for (Direction nextFacing : Iterate.directions) {
            if (nextFacing == currentFace.getFace())
                continue;
            Map<Direction, Boolean> map = pair.getSecond();
            if (!map.containsKey(nextFacing))
                continue;

            BlockFace localTarget = new BlockFace(currentPos, nextFacing);
            if (targets.contains(localTarget)) {
                validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
                        .add(localTarget);
                atLeastOneBranchSuccessful = true;
                continue;
            }

            if (map.get(nextFacing) != pull)
                continue;
            if (!searchForEndpointRecursively(pipeGraph, targets, validFaces,
                    new BlockFace(currentPos.relative(nextFacing), nextFacing.getOpposite()), pull))
                continue;

            validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
                    .add(localTarget);
            atLeastOneBranchSuccessful = true;
        }

        if (atLeastOneBranchSuccessful)
            validFaces.computeIfAbsent(distance, $ -> new HashSet<>())
                    .add(currentFace);

        return atLeastOneBranchSuccessful;
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

    public boolean isPullingOnSide(boolean front) {
        return !front;
    }

    @Override
    public boolean isCustomConnection(KineticBlockEntity other, BlockState state, BlockState otherState) {
        if (other instanceof CompressorGuideBlockEntity guideTarget) {
            CompressorCylinderBlockEntity cylinder = guideTarget.getCylinder();
            return cylinder != null && cylinder.equals(this);
        }
        return false;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            if (side != getFront())
                return LazyOptional.of(() -> tankInput).cast();
            else
                return LazyOptional.of(() -> tankOutput).cast();
        return super.getCapability(cap, side);
    }

    public static int getCapacityMultiplier() {
        return AllConfigs.server().fluids.fluidTankCapacity.get() * 1000;
    }

    class CompressorFluidTransferBehaviour extends PressurizedFluidTransportBehaviour {

        public CompressorFluidTransferBehaviour(SmartBlockEntity be) {
            super(be);
            sidesToUpdate = Couple.create(MutableBoolean::new);
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

//        @Override
//        public FluidStack getProvidedOutwardFluid(Direction side) {
//            FluidStack fluid = super.getProvidedOutwardFluid(side).copy();
//            if (fluid == null || fluid.isEmpty()) return fluid;
//            CompoundTag fluidTag = fluid.getOrCreateTag();
//            float pressure = fluidTag.contains("Pressure", Tag.TAG_FLOAT) ? fluidTag.getFloat("Pressure") : 1;
//            fluid.getTag().putFloat("Pressure", pressure * 2);
//            fluid.getTag().putBoolean("Hot", true);
////            fluid.setAmount(fluid.getAmount() / 2);
//            return fluid;
//        }
    }
}
