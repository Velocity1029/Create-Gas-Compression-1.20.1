package com.velocity1029.create_gas_compression.blocks.heat_exchanger;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.FluidTransformer;
import com.velocity1029.create_gas_compression.base.IFannable;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class CompressedGasCoolerBlockEntity extends IronPipeBlockEntity implements IFannable {

    // Fluid Handling
    public boolean shouldCool = false;
    private int coolCounter = 0;

    public CompressedGasCoolerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new CoolerFluidTransportBehaviour(this));

        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    @Override
    public void tick() {
        super.tick();
        if (shouldCool) {
            coolCounter--;
            if (coolCounter <= 0) {
                shouldCool = false;
            }
        }
    }

    @Override
    public void fan(AirCurrent airCurrent) {
        float speed = airCurrent.source.getSpeed();
        shouldCool = true;
        coolCounter = 5;
    }

    public FluidStack coolFluid(FluidStack fluid) {
        if (fluid.isEmpty() || !fluid.hasTag() || !shouldCool) return fluid;
        CompoundTag tags = fluid.getTag();
        boolean hot = tags.contains("Hot") && tags.getBoolean("Hot");
        tags.putBoolean("Hot", false);
        return fluid;
    }

    class CoolerFluidTransportBehaviour extends PressurizedPipeFluidTransportBehaviour implements FluidTransformer {

        public CoolerFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public FluidStack transformFluid(FluidStack fluid) {
            return coolFluid(fluid);
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return coolFluid(superFluid.copy());
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                        Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

            BlockPos offsetPos = pos.relative(direction);
            BlockState otherState = world.getBlockState(offsetPos);

            if (state.getBlock() instanceof EncasedPipeBlock && attachment != AttachmentTypes.DRAIN)
                return AttachmentTypes.NONE;

            if (attachment == AttachmentTypes.RIM) {
                if (!FluidPipeBlock.isPipe(otherState) && !(otherState.getBlock() instanceof EncasedPipeBlock)
                        && !(otherState.getBlock() instanceof GlassFluidPipeBlock)) {
                    FluidTransportBehaviour pipeBehaviour =
                            BlockEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
                    if (pipeBehaviour != null && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite()))
                        return AttachmentTypes.DETAILED_CONNECTION;
                }

                if (!FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
                    return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis()
                            ? AttachmentTypes.CONNECTION
                            : AttachmentTypes.DETAILED_CONNECTION;
            }

            if (attachment == AttachmentTypes.NONE
                    && state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
                return AttachmentTypes.DETAILED_CONNECTION;

            return attachment;
        }
    }
}
