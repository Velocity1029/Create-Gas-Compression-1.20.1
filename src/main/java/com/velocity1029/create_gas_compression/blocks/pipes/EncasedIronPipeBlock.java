package com.velocity1029.create_gas_compression.blocks.pipes;

import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.function.Supplier;

public class EncasedIronPipeBlock extends EncasedPipeBlock {
    public EncasedIronPipeBlock(Properties properties, Supplier<Block> casing) {
        super(properties, casing);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        return CGCBlocks.IRON_PIPE.asStack();
    }

    @Override
    public void handleEncasing(BlockState state, Level level, BlockPos pos, ItemStack heldItem, Player player, InteractionHand hand,
                               BlockHitResult ray) {
        BlockEntity entity = level.getBlockEntity(pos);
        FluidTransportBehaviour.cacheFlows(level, pos);
        level.setBlockAndUpdate(pos,
                EncasedIronPipeBlock.transferSixWayProperties(state, defaultBlockState()));
        FluidTransportBehaviour.loadFlows(level, pos);
        if (entity instanceof LockablePipeBlockEntity lockableEntity && level.getBlockEntity(pos) instanceof LockablePipeBlockEntity equivalentLockableEntity) {
            equivalentLockableEntity.locked = lockableEntity.locked;
        }
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (world.isClientSide)
            return InteractionResult.SUCCESS;

        context.getLevel()
                .levelEvent(2001, context.getClickedPos(), Block.getId(state));
        BlockState equivalentPipe = transferSixWayProperties(state, CGCBlocks.IRON_PIPE.getDefaultState());
        BlockEntity entity = world.getBlockEntity(pos);

        FluidTransportBehaviour.cacheFlows(world, pos);
        world.setBlockAndUpdate(pos, equivalentPipe);
        if (entity instanceof LockablePipeBlockEntity lockableEntity && world.getBlockEntity(pos) instanceof LockablePipeBlockEntity equivalentLockableEntity) {
            equivalentLockableEntity.locked = lockableEntity.locked;
        }
        FluidTransportBehaviour.loadFlows(world, pos);
        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntityType<? extends FluidPipeBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.ENCASED_IRON_PIPE.get();
    }
}
