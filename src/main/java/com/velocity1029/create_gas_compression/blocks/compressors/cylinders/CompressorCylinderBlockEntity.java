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
import com.simibubi.create.foundation.utility.CreateLang;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.mutable.MutableBoolean;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class CompressorCylinderBlockEntity extends PumpBlockEntity  {

//    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;

    public BlockPos guidePos;
    public float compressorEfficiency;
//    public int movementDirection;
    public int initialTicks;
    public static Block guideKey;

    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new PressurizedFluidTransportBehaviour(this));
        super.addBehaviours(behaviours);
//        behaviours.remove(getBehaviour(PumpFluidTransferBehaviour));
//        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
//        registerAwardables(behaviours, AllAdvancements.PUMP);

//        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
//                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new SteamEngineValueBox());
//        movementDirection.onlyActiveWhen(() -> {
//            CompressorGuideBlockEntity cylinder = getCylinder();
//            return cylinder == null || !cylinder.hasSource();
//        });
//        movementDirection.withCallback($ -> onDirectionChanged());
//        behaviours.add(movementDirection);
    }
    // TODO onDirectionChanged??
    private void onDirectionChanged() {
    }

    public void update(BlockPos sourcePos, float efficiency) {
        BlockPos key = worldPosition.subtract(sourcePos);
        guidePos = key;
        float prev = compressorEfficiency;
        compressorEfficiency = efficiency;
//        int prevDirection = this.movementDirection;
        if (Mth.equal(efficiency, prev)) //&& prevDirection == direction)
            return;

        setSpeed(efficiency);

        guideKey = level.getBlockState(sourcePos)
                .getBlock();
        BlockEntity blockEntity = level.getBlockEntity(sourcePos);
        if (blockEntity instanceof CompressorGuideBlockEntity guideBlockEntity) {
//            BlockEntity controller = level.getBlockEntity(guideBlockEntity.getFrame().getController());
            BlockEntity frame = guideBlockEntity.getFrame();
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

    @Override
    protected boolean isFront(Direction side) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return false;
        Direction front = blockState.getValue(CompressorCylinderBlock.FACING);
        boolean isFront = side == front;
        return isFront;
    }

    @Override
    @Nullable
    protected Direction getFront() {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return null;
        return blockState.getValue(CompressorCylinderBlock.FACING);
    }

    @Override
    public boolean isSideAccessible(Direction side) {
        BlockState blockState = getBlockState();
        if (!(blockState.getBlock() instanceof CompressorCylinderBlock))
            return false;
        return blockState.getValue(CompressorCylinderBlock.FACING)
                .getAxis() == side.getAxis();
    }


    class CompressorFluidTransferBehaviour extends FluidTransportBehaviour {

        public CompressorFluidTransferBehaviour(SmartBlockEntity be) {
            super(be);
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

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack fluid = super.getProvidedOutwardFluid(side);

            return fluid;
        }
    }
}
