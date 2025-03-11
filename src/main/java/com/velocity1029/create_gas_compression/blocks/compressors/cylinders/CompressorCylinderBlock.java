package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlockEntity;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import static net.minecraft.world.level.block.Rotation.CLOCKWISE_90;
import static net.minecraft.world.level.block.Rotation.COUNTERCLOCKWISE_90;

public class CompressorCylinderBlock extends Block implements ProperWaterloggedBlock, IBE<CompressorCylinderBlockEntity> {

    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;

    public CompressorCylinderBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(BlockStateProperties.AXIS, Direction.Axis.Y)
                .setValue(BlockStateProperties.WATERLOGGED, false));
        this.registerImpact(defaultBlockState());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(AXIS, WATERLOGGED));
    }

    private void registerImpact(BlockState state) {
        BlockStressValues.IMPACTS.register(state.getBlock(), () -> {return 2048;});
    }

    public BlockState rotate(BlockState state, Rotation rot) {
        switch (rot) {
            case COUNTERCLOCKWISE_90:
            case CLOCKWISE_90:
                switch (state.getValue(AXIS)) {
                    case X:
                        return state.setValue(AXIS, Direction.Axis.Z);
                    case Z:
                        return state.setValue(AXIS, Direction.Axis.X);
                    default:
                        return state;
                }
            default:
                return state;
        }
    }

    public static Axis getPreferredAxis(BlockPlaceContext context) {
        Axis prefferedAxis = null;
        for (Direction side : Iterate.directions) {
            BlockState blockState = context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(side));
            if (blockState.getBlock() instanceof CompressorGuideBlock) {
//                if (((CompressorGuideBlock) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
//                        .relative(side), blockState, side.getOpposite()))
                if ((CompressorGuideBlock.getConnectedDirection(blockState)) == side)
                    if (prefferedAxis != null && prefferedAxis != side.getAxis()) {
                        prefferedAxis = null;
                        break;
                    } else {
                        if (side.getAxis() == Axis.Y)
                            prefferedAxis = Axis.X;
                        else
                            prefferedAxis = Axis.Y;
                    }
            }
        }
        return prefferedAxis;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Axis preferredAxis = getPreferredAxis(context);
        if (preferredAxis != null && (context.getPlayer() == null || !context.getPlayer()
                .isShiftKeyDown()))
            return this.defaultBlockState()
                    .setValue(AXIS, preferredAxis);
        return this.defaultBlockState()
                .setValue(AXIS, preferredAxis != null && context.getPlayer()
                        .isShiftKeyDown() ? context.getClickedFace()
                        .getAxis()
                        : context.getNearestLookingDirection()
                        .getAxis());
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

    @Override
    public Class<CompressorCylinderBlockEntity> getBlockEntityClass() {
        return CompressorCylinderBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompressorCylinderBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_CYLINDER.get();
    }
}
