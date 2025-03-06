package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BehaviourType;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class IronPipeBlockEntity extends FluidPipeBlockEntity {

    public IronPipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new PressurizedPipeFluidTransportBehaviour(this));
        behaviours.add(new BracketedBlockEntityBehaviour(this, this::canHaveBracket));
//        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    private boolean canHaveBracket(BlockState state) {
        return !(state.getBlock() instanceof EncasedPipeBlock);
    }

    class PressurizedPipeFluidTransportBehaviour extends FluidTransportBehaviour {

        public static final BehaviourType<PressurizedPipeFluidTransportBehaviour> TYPE = new BehaviourType<>();

        public PressurizedPipeFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return (IronPipeBlock.isPressurizedPipe(state) || state.getBlock() instanceof EncasedPipeBlock)
                    && state.getValue(IronPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
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
                if (!IronPipeBlock.isPressurizedPipe(otherState) && !(otherState.getBlock() instanceof EncasedPipeBlock)
                        && !(otherState.getBlock() instanceof GlassIronPipeBlock)) {
                    PressurizedPipeFluidTransportBehaviour pipeBehaviour =
                            BlockEntityBehaviour.get(world, offsetPos, PressurizedPipeFluidTransportBehaviour.TYPE);
                    if (pipeBehaviour != null && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite()))
                        return AttachmentTypes.DETAILED_CONNECTION;
                }

                if (!IronPipeBlock.shouldDrawRim(world, pos, state, direction))
                    return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis()
                            ? AttachmentTypes.CONNECTION
                            : AttachmentTypes.DETAILED_CONNECTION;
            }

            if (attachment == AttachmentTypes.NONE
                    && state.getValue(IronPipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
                return AttachmentTypes.DETAILED_CONNECTION;

            return attachment;
        }
    }
}
