package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.FluidTransformer;
import com.velocity1029.create_gas_compression.base.PressurizedFluidDistribution;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.*;

public class CompressorCylinderBlockEntity extends PumpBlockEntity {

    Couple<MutableBoolean> sidesToUpdate;
    boolean pressureUpdate;

    public BlockPos guidePos;
    public float compressorEfficiency;
    public int initialTicks;
    public static Block guideKey;

    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
        sidesToUpdate = Couple.create(MutableBoolean::new);
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

    public boolean isFront(Direction side) {
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

    protected void distributePressureTo(Direction side) {
        if (getSpeed() == 0)
            return;
        boolean pull = isPullingOnSide(isFront(side));

        float pressure = Math.abs(getSpeed());
        float correctedPressure = pull? pressure : pressure / 2;

        PressurizedFluidDistribution.distributePressureTo(level, worldPosition, side, correctedPressure, pull);
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

    protected static FluidStack pressurizeFluid(FluidStack fluid) {
        if (fluid.isEmpty()) return fluid;
        CompoundTag tags = fluid.getOrCreateTag();
        if (tags.contains("Hot", Tag.TAG_BYTE) && tags.getBoolean("Hot")) return FluidStack.EMPTY;
        float pressure = tags.contains("Pressure", Tag.TAG_FLOAT) ? tags.getFloat("Pressure") : 1;
        tags.putFloat("Pressure", pressure * 2f);
        tags.putBoolean("Hot", true);
        return fluid;
    }

    public class CompressorFluidTransferBehaviour extends PressurizedFluidTransportBehaviour implements FluidTransformer {

        public CompressorFluidTransferBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public FluidStack transformFluid(FluidStack fluid) {
            pressurizeFluid(fluid);
            fluid.setAmount(fluid.getAmount() / 2);
            return fluid;
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return pressurizeFluid(superFluid.copy());
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
}
