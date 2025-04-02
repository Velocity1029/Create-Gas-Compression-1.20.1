package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.ticks.TickPriority;

public class LockablePipeBlockEntity extends FluidPipeBlockEntity {

    public boolean locked = false;

    public LockablePipeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void toggleLock(Player player){
        level.playSound(null, getBlockPos(), SoundEvents.COPPER_STEP, SoundSource.BLOCKS, 0.6f,0.5f);
        locked = !locked;
        BlockPos pos = getBlockPos();
        level.scheduleTick(pos, this.getBlockState().getBlock(), 1, TickPriority.HIGH);
        if(locked)
            return;

        BlockState newState;
        FluidTransportBehaviour.cacheFlows(level, pos);
        newState = updatePipe(level, pos, getBlockState());
        level.setBlock(pos, newState, 3);
        FluidTransportBehaviour.loadFlows(level, pos);
    }

    public BlockState updatePipe(LevelAccessor world, BlockPos pos, BlockState state) {
        Direction side = Direction.UP;
        return AllBlocks.FLUID_PIPE.get()
                .updateBlockState(
                        transferSixWayProperties(state, state.getBlock().defaultBlockState())
                                .setValue(BlockStateProperties.WATERLOGGED, state.getValue(BlockStateProperties.WATERLOGGED)),
                        side, null, world, pos);
    }

    public static BlockState transferSixWayProperties(BlockState from, BlockState to) {
        for (Direction d : Iterate.directions) {
            BooleanProperty property = PipeBlock.PROPERTY_BY_DIRECTION.get(d);
            to = to.setValue(property, from.getValue(property));
        }
        return to;
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        compound.putBoolean("Locked", locked);

        super.write(compound, clientPacket);

    }
    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        locked = compound.getBoolean("Locked");

    }
}