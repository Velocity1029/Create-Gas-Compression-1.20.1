package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.VanillaFluidTargets;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.blocks.pipes.LockablePipeBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;
import java.util.Arrays;

import static com.simibubi.create.content.fluids.pipes.FluidPipeBlock.*;
import static net.minecraft.world.level.block.PipeBlock.PROPERTY_BY_DIRECTION;

@Mixin(FluidPipeBlock.class)
public class FluidPipeBlockMixin {
    /**
     * @author Velocity1029/DrMangoTea
     * @reason Lockable pipes
     */
    @Overwrite(remap = false)
    public BlockState updateBlockState(BlockState state, Direction preferredDirection, @Nullable Direction ignore,
                                       BlockAndTintGetter world, BlockPos pos) {

        BracketedBlockEntityBehaviour bracket = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (bracket != null && bracket.isBracketPresent())
            return state;

        BlockState prevState = state;
        int prevStateSides = (int) Arrays.stream(Iterate.directions)
                .map(PROPERTY_BY_DIRECTION::get)
                .filter(prevState::getValue)
                .count();

        // Update sides that are not ignored
        for (Direction d : Iterate.directions)
            if (d != ignore) {
                boolean shouldConnect = canConnectTo(world, pos.relative(d), world.getBlockState(pos.relative(d)), d);
                state = state.setValue(PROPERTY_BY_DIRECTION.get(d), shouldConnect);
            }

        // See if it has enough connections
        Direction connectedDirection = null;
        for (Direction d : Iterate.directions) {
            if (isOpenAt(state, d)) {
                if (connectedDirection != null)
                    return state;
                connectedDirection = d;
            }
        }

        // Add opposite end if only one connection
        if (connectedDirection != null)
            return state.setValue(PROPERTY_BY_DIRECTION.get(connectedDirection.getOpposite()), true);

        // If we can't connect to anything and weren't connected before, do nothing
        if (prevStateSides == 2)
            return prevState;

        // Use preferred
        return state.setValue(PROPERTY_BY_DIRECTION.get(preferredDirection), true)
                .setValue(PROPERTY_BY_DIRECTION.get(preferredDirection.getOpposite()), true);
    }

    /**
     * @author Velocity1029
     * @reason Lockable pipes
     */
    @Overwrite(remap = false)
    public static boolean canConnectTo(BlockAndTintGetter world, BlockPos neighbourPos, BlockState neighbour,
                                       Direction direction) {
        if (FluidPropagator.hasFluidCapability(world, neighbourPos, direction.getOpposite()))
            return true;
        if (VanillaFluidTargets.canProvideFluidWithoutCapability(neighbour))
            return true;
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, neighbourPos, FluidTransportBehaviour.TYPE);
        BracketedBlockEntityBehaviour bracket =
                BlockEntityBehaviour.get(world, neighbourPos, BracketedBlockEntityBehaviour.TYPE);
        if (isPipe(neighbour))
            return (bracket == null || !bracket.isBracketPresent()
                    || FluidPropagator.getStraightPipeAxis(neighbour) == direction.getAxis())
                    && (!(world.getBlockEntity(neighbourPos) instanceof LockablePipeBlockEntity lockablePipeBlockEntity)
                    || !lockablePipeBlockEntity.locked
                    || neighbour.getValue(PROPERTY_BY_DIRECTION.get(direction.getOpposite())));
        if (transport == null)
            return false;
        return transport.canHaveFlowToward(neighbour, direction.getOpposite());
    }
}
