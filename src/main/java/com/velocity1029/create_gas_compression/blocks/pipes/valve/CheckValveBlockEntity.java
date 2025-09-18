package com.velocity1029.create_gas_compression.blocks.pipes.valve;

import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.SmartFluidPipeBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.blocks.pipes.PressurizedStraightPipeBlockEntity.PressurizedStraightPipeFluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class CheckValveBlockEntity extends SmartFluidPipeBlockEntity {


    public CheckValveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new CheckValvePipeBehaviour(this));
        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    class CheckValvePipeBehaviour extends PressurizedStraightPipeFluidTransportBehaviour {

        public CheckValvePipeBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return state.getValue(CheckValveBlock.FLOW).getAxis() == direction.getAxis();
        }

        @Override
        public boolean canPullFluidFrom(FluidStack fluid, BlockState state, Direction direction) {
            if (state.getValue(CheckValveBlock.FLOW).getOpposite() == direction)
                return super.canPullFluidFrom(fluid, state, direction);
            return false;
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
        public void addPressure(Direction side, boolean inbound, float pressure) {
            createConnectionData();
            if (!interfaces.containsKey(side))
                return;
            // TODO bandaid fix might be problematic
            if (blockEntity.getBlockState().getValue(CheckValveBlock.FLOW) == side ^ inbound)
                interfaces.get(side)
                    .addPressure(inbound, pressure);
            blockEntity.sendData();
        }

    }
}
