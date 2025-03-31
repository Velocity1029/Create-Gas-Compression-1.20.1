package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;

public class CompressorCylinderBlock extends PumpBlock {

    public static final EnumProperty<Direction> ATTACHED_FACE = DirectionProperty.create("attached", Direction.values());

    public CompressorCylinderBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(ATTACHED_FACE, Direction.SOUTH)
                .setValue(BlockStateProperties.WATERLOGGED, false));
        this.registerImpact(defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(ATTACHED_FACE));
    }

    private void registerImpact(BlockState state) {
        BlockStressValues.IMPACTS.register(state.getBlock(), () -> {return 2048;});
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
                return state.setValue(FACING, state.getValue(FACING).getCounterClockWise(state.getValue(ATTACHED_FACE).getAxis()));
            case CLOCKWISE_90:
                return state.setValue(FACING, state.getValue(FACING).getClockWise(state.getValue(ATTACHED_FACE).getAxis()));
            default:
                return state;
        }
    }

    @Override
    public BlockState getRotatedBlockState(BlockState originalState, Direction targetedFace) {
        if (targetedFace.getAxis() == originalState.getValue(ATTACHED_FACE).getAxis())
            return originalState.setValue(FACING, originalState.getValue(FACING).getClockWise(originalState.getValue(ATTACHED_FACE).getAxis()));
        return originalState.setValue(FACING, originalState.getValue(FACING)
                .getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState toPlace = super.getStateForPlacement(context);
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        Direction nearestLookingDirection = context.getNearestLookingDirection();
        Direction targetDirection = context.getPlayer() != null && context.getPlayer()
                .isShiftKeyDown() ? nearestLookingDirection : nearestLookingDirection.getOpposite();
        Direction bestConnectedDirection = null;
        double bestDistance = Double.MAX_VALUE;

        for (Direction d : Iterate.directions) {
            BlockPos adjPos = pos.relative(d);
            BlockState adjState = level.getBlockState(adjPos);
            if (!(adjState.getBlock() instanceof CompressorGuideBlock))
                continue;
            double distance = Vec3.atLowerCornerOf(d.getNormal())
                    .distanceTo(Vec3.atLowerCornerOf(targetDirection.getNormal()));
            if (distance > bestDistance)
                continue;
            bestDistance = distance;
            bestConnectedDirection = d;
        }

        Direction facing = toPlace.getValue(FACING);
        if (bestConnectedDirection == null)
            return toPlace.setValue(ATTACHED_FACE, facing.getAxis() != Axis.Y ? facing.getClockWise() : Direction.NORTH);
        if (bestConnectedDirection.getAxis() == facing.getAxis())
            return toPlace.setValue(ATTACHED_FACE, bestConnectedDirection).setValue(FACING, facing.getClockWise());
//        if (player.isShiftKeyDown() && bestConnectedDirection.getAxis() != targetDirection.getAxis())
//            return toPlace;

        return toPlace.setValue(ATTACHED_FACE, bestConnectedDirection);
    }

    @Override
    public boolean isSmallCog() {
        return false;
    }

    //    public static BlockState getEquivalent(BlockState stateForPlacement) {
//        return CGCBlocks.COMPRESSOR_CYLINDER.getDefaultState()
//                .setValue(CompressorCylinderBlock.AXIS, stateForPlacement.getValue(CompressorCylinderBlock.AXIS))
//                .setValue(WATERLOGGED, stateForPlacement.getValue(WATERLOGGED));
//    }

//    @Override
//    public Class<CompressorGuideBlockEntity> getBlockEntityClass() {
//        return CompressorGuideBlockEntity.class;
//    }

//    @Override
//    public Class<CompressorCylinderBlockEntity> getBlockEntityClass() {
//        return CompressorCylinderBlockEntity.class;
//    }

    @Override
    public BlockEntityType<? extends CompressorCylinderBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_CYLINDER.get();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(ATTACHED_FACE)
                .getAxis();
    }
}
