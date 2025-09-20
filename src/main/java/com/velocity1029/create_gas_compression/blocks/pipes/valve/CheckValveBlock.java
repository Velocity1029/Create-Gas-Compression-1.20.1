package com.velocity1029.create_gas_compression.blocks.pipes.valve;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.IAxisPipe;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import com.velocity1029.create_gas_compression.registry.CGCShapes;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.math.VoxelShaper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;


public class CheckValveBlock extends Block implements IAxisPipe, IWrenchable, IBE<CheckValveBlockEntity>, ProperWaterloggedBlock {
    public static final DirectionProperty VALVE;
    public static final DirectionProperty FLOW;

    public CheckValveBlock(Properties p_52591_) {
        super(p_52591_);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
        builder.add(VALVE, FLOW, WATERLOGGED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction[] var2 = ctx.getNearestLookingDirections();
        int var3 = var2.length;

        BlockState stateForPlacement = this.defaultBlockState();
        for(int var4 = 0; var4 < var3; ++var4) {
            Direction $$1 = var2[var4];
            BlockState $$3;
            if ($$1.getAxis() == Axis.Y) {
                $$3 = (BlockState)((BlockState)this.defaultBlockState().setValue(VALVE, $$1 == Direction.UP ? Direction.DOWN : Direction.UP)).setValue(FLOW, ctx.getHorizontalDirection());
            } else {
                $$3 = (BlockState)((BlockState)this.defaultBlockState().setValue(VALVE, $$1.getOpposite())).setValue(FLOW, ctx.getNearestLookingVerticalDirection());
            }

            if ($$3.canSurvive(ctx.getLevel(), ctx.getClickedPos())) {
                stateForPlacement = $$3;
                break;
            }
        }

        Axis prefferedAxis = null;
        BlockPos pos = ctx.getClickedPos();
        Level world = ctx.getLevel();
        for (Direction side : Iterate.directions) {
            if (!prefersConnectionTo(world, pos, side))
                continue;
            if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                prefferedAxis = null;
                break;
            }
            prefferedAxis = side.getAxis();
        }

        if (prefferedAxis == Direction.Axis.Y)
            stateForPlacement = stateForPlacement.setValue(VALVE, ctx.getHorizontalDirection().getOpposite())
                    .setValue(FLOW, ctx.getNearestLookingVerticalDirection());
        else if (prefferedAxis != null) {
            if (stateForPlacement.getValue(VALVE).getAxis().isHorizontal())
                stateForPlacement = stateForPlacement.setValue(VALVE, Direction.UP);
            for (Direction direction : ctx.getNearestLookingDirections()) {
                if (direction.getAxis() != prefferedAxis)
                    continue;
                stateForPlacement = stateForPlacement.setValue(FLOW, direction.getOpposite());
            }
        }

        return withWater(stateForPlacement, ctx);
    }

    protected boolean prefersConnectionTo(LevelReader reader, BlockPos pos, Direction facing) {
        BlockPos offset = pos.relative(facing);
        BlockState blockState = reader.getBlockState(offset);
        return FluidPipeBlock.canConnectTo(reader, offset, blockState, facing);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        boolean blockTypeChanged = state.getBlock() != newState.getBlock();
        if (blockTypeChanged && !world.isClientSide)
            FluidPropagator.propagateChangedPipe(world, pos, state);
        IBE.onRemove(state, world, pos, newState);
    }

    @Override
    public boolean canSurvive(BlockState p_196260_1_, LevelReader p_196260_2_, BlockPos p_196260_3_) {
        return true;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (world.isClientSide)
            return;
        if (state != oldState)
            world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block otherBlock, BlockPos neighborPos,
                                boolean isMoving) {
        DebugPackets.sendNeighborsUpdatePacket(world, pos);
        Direction d = FluidPropagator.validateNeighbourChange(state, world, pos, otherBlock, neighborPos, isMoving);
        if (d == null)
            return;
        if (!isOpenAt(state, d))
            return;
        world.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    public static boolean isOpenAt(BlockState state, Direction d) {
        return d.getAxis() == getPipeAxis(state);
    }

    @Override
    public void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource r) {
        FluidPropagator.propagateChangedPipe(world, pos, state);
    }

    protected static Axis getPipeAxis(BlockState state) {
        return state.getValue(FLOW).getAxis();
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter p_220053_2_, BlockPos p_220053_3_,
                               CollisionContext p_220053_4_) {
        Direction face = state.getValue(VALVE);
        VoxelShaper shape = face == Direction.UP ? CGCShapes.CHECK_VALVE_FLOOR
                : face == Direction.DOWN ? CGCShapes.CHECK_VALVE_CEILING : CGCShapes.CHECK_VALVE_WALL;
        return shape == CGCShapes.CHECK_VALVE_WALL ? shape.get(state.getValue(VALVE)) : shape.get(state.getValue(FLOW));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public Axis getAxis(BlockState state) {
        return getPipeAxis(state);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel,
                                  BlockPos pCurrentPos, BlockPos pFacingPos) {
        updateWater(pLevel, pState, pCurrentPos);
        return pState;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return fluidState(pState);
    }

    @Override
    public Class<CheckValveBlockEntity> getBlockEntityClass() {
        return CheckValveBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CheckValveBlockEntity> getBlockEntityType() {
        return  CGCBlockEntities.CHECK_VALVE.get();
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return (BlockState)pState.setValue(FLOW, pRot.rotate((Direction)pState.getValue(FLOW)));
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation((Direction)pState.getValue(FLOW)));
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        BlockState newState = originalState;

        if (!originalState.hasProperty(FLOW))
            return originalState;

        Direction stateFacing = originalState.getValue(FLOW);
        Direction valveFacing = originalState.getValue(VALVE);

        if (stateFacing.getAxis()
                .equals(targetedFace.getAxis())) {
            if (stateFacing.getAxis().isVertical())
                return originalState.setValue(VALVE, valveFacing.getClockWise(targetedFace.getAxis()));
            return originalState;
        }
        if (stateFacing.getAxis().isVertical() && valveFacing.getAxis().equals(targetedFace.getAxis()))
            return originalState;
        do {
            newState = newState.setValue(FLOW, newState.getValue(FLOW).getClockWise(targetedFace.getAxis()));
            if (!targetedFace.getAxis().equals(newState.getValue(VALVE).getAxis()))
                newState = newState.setValue(VALVE, newState.getValue(VALVE).getClockWise(targetedFace.getAxis()));
        } while (newState.getValue(FLOW)
                .getAxis()
                .equals(targetedFace.getAxis()));
        return newState;
    }

    static {
        VALVE = DirectionProperty.create("valve", new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.UP, Direction.DOWN});
        FLOW = BlockStateProperties.FACING;
    }
}
