package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.PipeConnection;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.data.Couple;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.List;
import java.util.Map;

public class CompressorCylinderBlockEntity extends PumpBlockEntity  {

    public BlockPos guidePos;
    public float compressorEfficiency;
    public int movementDirection;
    public int initialTicks;
    public static Block guideKey;

    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
        super(typeIn, pos, state);
    }

    public void update(BlockPos sourcePos, int direction, float efficiency) {
        BlockPos key = worldPosition.subtract(sourcePos);
        guidePos = key;
        float prev = compressorEfficiency;
        compressorEfficiency = efficiency;
        int prevDirection = this.movementDirection;
        if (Mth.equal(efficiency, prev) && prevDirection == direction)
            return;

        guideKey = level.getBlockState(sourcePos)
                .getBlock();
        this.movementDirection = direction;
//        // TODO make pump equivalent of updateGeneratedRotation in PoweredShaftBlockEntity
//        updatepumpfluidspeeddirectionstuffandthings();
//
    }

    public void remove(BlockPos sourcePos) {
        if (!isPoweredBy(sourcePos))
            return;

        guidePos = null;
        compressorEfficiency = 0;
        movementDirection = 0;
        guideKey = null;
//        updatepumpfluidspeeddirectionstuffandthings();
    }

    public boolean canBePoweredBy(BlockPos globalPos) {
        return initialTicks == 0 && (guidePos == null || isPoweredBy(globalPos));
    }

    public boolean isPoweredBy(BlockPos globalPos) {
        BlockPos key = worldPosition.subtract(globalPos);
        return key.equals(guidePos);
    }

////    PumpBlockEntity pump = new PumpBlockEntity(this.getType(), worldPosition, getBlockState());
//    Couple<MutableBoolean> sidesToUpdate;
//    boolean scheduleFlip;
//
//    public CompressorCylinderBlockEntity(BlockEntityType<?> typeIn, BlockPos pos, BlockState state) {
//        super(typeIn, pos, state);
//        sidesToUpdate = Couple.create(MutableBoolean::new);
//    }
//
//    @Override
//    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//        super.addBehaviours(behaviours);
//        behaviours.add(new PumpFluidTransferBehaviour(this));
////        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
////        registerAwardables(behaviours, AllAdvancements.PUMP);
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//
//        if (level.isClientSide && !isVirtual())
//            return;
//
//        if (scheduleFlip) {
//            level.setBlockAndUpdate(worldPosition,
//                    getBlockState().setValue(CompressorCylinderBlock.FACING, getBlockState().getValue(CompressorCylinderBlock.FACING)
//                            .getOpposite()));
//            scheduleFlip = false;
//        }
//
//        sidesToUpdate.forEachWithContext((update, isFront) -> {
//            if (update.isFalse())
//                return;
//            update.setFalse();
//            distributePressureTo(isFront ? getFront() : getFront().getOpposite());
//        });
//    }
//
//
//
//
//
//
//    class PumpFluidTransferBehaviour extends FluidTransportBehaviour {
//
//        public PumpFluidTransferBehaviour(SmartBlockEntity be) {
//            super(be);
//        }
//
//        @Override
//        public void tick() {
//            super.tick();
//            for (Map.Entry<Direction, PipeConnection> entry : interfaces.entrySet()) {
//                boolean pull = isPullingOnSide(isFront(entry.getKey()));
//                Couple<Float> pressure = entry.getValue().getPressure();
//                pressure.set(pull, Math.abs(getSpeed()));
//                pressure.set(!pull, 0f);
//            }
//        }
//
//        @Override
//        public boolean canHaveFlowToward(BlockState state, Direction direction) {
//            return isSideAccessible(direction);
//        }
//
//        @Override
//        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
//                                                        Direction direction) {
//            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
//            if (attachment == AttachmentTypes.RIM)
//                return AttachmentTypes.NONE;
//            return attachment;
//        }
//
//    }
}
