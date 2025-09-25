package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.AxisPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.IAxisPipe;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class PressurizedStraightPipeBlockEntity extends SmartBlockEntity {

    public PressurizedStraightPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new PressurizedStraightPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this));
        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    public static class PressurizedStraightPipeFluidTransportBehaviour extends PressurizedFluidTransportBehaviour {

        public PressurizedStraightPipeFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return state.hasProperty(AxisPipeBlock.AXIS) && state.getValue(AxisPipeBlock.AXIS) == direction.getAxis();
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                        Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);
            BlockState otherState = world.getBlockState(pos.relative(direction));

            Direction.Axis axis = IAxisPipe.getAxisOf(state);
            Direction.Axis otherAxis = IAxisPipe.getAxisOf(otherState);

            if (attachment == AttachmentTypes.RIM && state.getBlock() instanceof FluidValveBlock)
                return AttachmentTypes.NONE;
            if (attachment == AttachmentTypes.RIM && !(state.getBlock() instanceof GlassFluidPipeBlock)
                    && otherState.getBlock() instanceof GlassFluidPipeBlock)
                return AttachmentTypes.PARTIAL_RIM;

            if (attachment == AttachmentTypes.RIM && FluidPipeBlock.isPipe(otherState))
                return AttachmentTypes.NONE;
            if (axis == otherAxis && axis != null)
                return AttachmentTypes.NONE;

            if (otherState.getBlock() instanceof FluidValveBlock
                    && FluidValveBlock.getPipeAxis(otherState) == direction.getAxis())
                return AttachmentTypes.NONE;

            return attachment.withoutConnector();
        }

    }
}
