package com.velocity1029.create_gas_compression.blocks.tanks;

import static java.lang.Math.abs;

import java.util.*;

import javax.annotation.Nullable;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.velocity1029.create_gas_compression.base.PressurizedFluidDistribution;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankBlock.Shape;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.advancement.AllAdvancements;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;

import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

public class IronTankBlockEntity extends FluidTankBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer.Fluid {

    static final float maxDistance = CreateGasCompressionConfig.getServer().pressurizedFluidRange.get();
    static final float maxPressure = (float) Math.pow(2, CreateGasCompressionConfig.getServer().maximumPressureStages.get());

    Map<Direction, MutableBoolean> sidesToUpdate;
    boolean pressureUpdate;
    protected float fluidInducedPressure;

    public IronTankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        sidesToUpdate = new HashMap<>();
        for (Direction direction : Iterate.directions) sidesToUpdate.put(direction, new MutableBoolean(false));
        fluidInducedPressure = 0;
        tankInventory = createInventory();
        fluidCapability = LazyOptional.of(() -> tankInventory);
    }

    protected SmartFluidTank createInventory() {
        return new SmartFluidTank(getCapacityMultiplier(), this::onFluidStackChanged);
    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide)
            return;
        if (!isController())
            return;
        ConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (pressureUpdate) {
            updatePressureChange();
        }

        sidesToUpdate.forEach((direction, update) -> {
            if (update.isFalse())
                return;
            update.setFalse();
            PressurizedFluidDistribution.distributePressureTo(level, worldPosition, direction, fluidInducedPressure, false);
        });
    }

    protected void onFluidStackChanged(FluidStack newFluidStack) {
        if (!hasLevel())
            return;

        super.onFluidStackChanged(newFluidStack);

        float oldPressure = fluidInducedPressure;
        float newPressure = getPressure();

        if (level.isClientSide && !isVirtual())
            return;
        if (oldPressure != newPressure)
            pressureUpdate = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IronTankBlockEntity getControllerBE() {
        if (isController() || !hasLevel())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof IronTankBlockEntity)
            return (IronTankBlockEntity) blockEntity;
        return null;
    }

    public void setWindows(boolean window) {
        this.window = window;
        for (int yOffset = 0; yOffset < height; yOffset++) {
            for (int xOffset = 0; xOffset < width; xOffset++) {
                for (int zOffset = 0; zOffset < width; zOffset++) {

                    BlockPos pos = this.worldPosition.offset(xOffset, yOffset, zOffset);
                    BlockState blockState = level.getBlockState(pos);
                    if (!IronTankBlock.isTank(blockState))
                        continue;

                    Shape shape = Shape.PLAIN;
                    if (window) {
                        // SIZE 1: Middle height tank has a window
                        if (width == 1 && yOffset == height / 2)
                            shape = Shape.WINDOW;
                        // SIZE 2: No windows
//                        if (width == 2)
//                            shape = xOffset == 0 ? zOffset == 0 ? Shape.WINDOW_NW : Shape.WINDOW_SW
//                                    : zOffset == 0 ? Shape.WINDOW_NE : Shape.WINDOW_SE;
                        // SIZE 3: Tanks in the center have a window
                        if (width == 3 && abs(abs(xOffset) - abs(zOffset)) == 1 && yOffset == height / 2 )
                            shape = Shape.WINDOW;
                    }

                    level.setBlock(pos, blockState.setValue(IronTankBlock.SHAPE, shape), 22);
                    level.getChunkSource()
                            .getLightEngine()
                            .checkBlock(pos);
                }
            }
        }
    }

    @Nullable
    public IronTankBlockEntity getOtherIronTankBlockEntity(Direction direction) {
        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
        if (otherBE instanceof IronTankBlockEntity)
            return (IronTankBlockEntity) otherBE;
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) { //TODO advancements
        registerAwardables(behaviours, AllAdvancements.STEAM_ENGINE_MAXED, AllAdvancements.PIPE_ORGAN);
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (IronTankBlock.isTank(state)) { // safety
            state = state.setValue(IronTankBlock.BOTTOM, getController().getY() == getBlockPos().getY());
            state = state.setValue(IronTankBlock.TOP, getController().getY() + height - 1 == getBlockPos().getY());
            level.setBlock(getBlockPos(), state, 6);
        }
        if (isController())
            setWindows(window);
        onFluidStackChanged(tankInventory.getFluid());
        updateBoilerState();
        setChanged();
    }

    public float getPressure() {
        CompoundTag fluidTags = tankInventory.getFluid().getTag();
        if (!tankInventory.getFluid().isEmpty() &&
                fluidTags != null &&
                fluidTags.contains("Pressure", Tag.TAG_FLOAT) &&
                fluidTags.getFloat("Pressure") > 1)
            fluidInducedPressure = fluidTags.getFloat("Pressure") / maxPressure * maxDistance;
        else
            fluidInducedPressure = 0;
        return fluidInducedPressure;
    }

    public void updatePressureChange() {
        pressureUpdate = false;
//        FluidPropagator.propagateChangedPipe(level, worldPosition, getBlockState());
        for (Direction direction : Iterate.directions) {
            BlockPos pos = worldPosition.relative(direction);
            FluidTransportBehaviour pipe = FluidPropagator.getPipe(level, pos);
            if (pipe != null) {
                PipeConnection connection = pipe.getConnection(direction.getOpposite());
                if (connection != null && connection.getPressure().getSecond() == 0 || connection.getPressure().getFirst() != 0)
                    FluidPropagator.propagateChangedPipe(level, pos, level.getBlockState(pos));
            }
        }
        sidesToUpdate.forEach((direction, update) -> update.setTrue());
    }

    public void updatePipesOnSide(Direction side) {
        MutableBoolean update = sidesToUpdate.get(side);
        update.setTrue();
//        wipeAdjacentPressures();
    }

    public void wipeAdjacentPressures() {
        for (Direction direction : Iterate.directions) {
            BlockEntity entity = level.getBlockEntity(worldPosition.relative(direction));
            if (entity instanceof SmartBlockEntity smartBlockEntity) {
                FluidTransportBehaviour behaviour = smartBlockEntity.getBehaviour(FluidTransportBehaviour.TYPE);
                if (behaviour != null)
                    behaviour.wipePressure();
            }
        }
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);
        getPressure();
        pressureUpdate = true;
    }
}