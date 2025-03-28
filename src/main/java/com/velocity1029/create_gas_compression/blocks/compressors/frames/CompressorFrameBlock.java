package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.kinetics.base.*;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.ProperWaterloggedBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.CompressorConnectivityHandler;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import net.createmod.catnip.data.Iterate;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class CompressorFrameBlock extends HorizontalKineticBlock implements IBE<CompressorFrameBlockEntity>, ProperWaterloggedBlock {

    public static final BooleanProperty START = BooleanProperty.create("start");
    public static final BooleanProperty END = BooleanProperty.create("end");
    public static final EnumProperty<Shape> SHAPE = EnumProperty.create("shape", Shape.class);


//    static public CompressorFrameBlockEntity blockEntity;

    public CompressorFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(WATERLOGGED, false).setValue(START, true).setValue(END, true).setValue(SHAPE, Shape.CONTROLLER));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(START, END, SHAPE, WATERLOGGED));
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean moved) {
        if (oldState.getBlock() == state.getBlock())
            return;
        if (moved)
            return;
        withBlockEntityDo(world, pos, CompressorFrameBlockEntity::updateConnectivity);
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.hasBlockEntity() && (state.getBlock() != newState.getBlock() || !newState.hasBlockEntity())) {
            BlockEntity be = world.getBlockEntity(pos);
            if (!(be instanceof CompressorFrameBlockEntity frameBE))
                return;
            world.removeBlockEntity(pos);
            CompressorConnectivityHandler.splitMulti(frameBE);
        }
    }

    //TODO link up with existing frames better
    public Direction getPreferredHorizontalFacing(BlockPlaceContext context) {
        Direction prefferedSide = null;
        for (Direction side : Iterate.horizontalDirections) {
            BlockState blockState = context.getLevel()
                    .getBlockState(context.getClickedPos()
                            .relative(side));
            if (blockState.getBlock() instanceof CompressorFrameBlock) {
                if (blockState.getValue(HORIZONTAL_FACING) == side.getOpposite()) {
                    prefferedSide = side;
                }
                continue;
            }
            if (blockState.getBlock() instanceof IRotate) {
                if (((IRotate) blockState.getBlock()).hasShaftTowards(context.getLevel(), context.getClickedPos()
                        .relative(side), blockState, side.getOpposite()))
                    if (prefferedSide != null && prefferedSide.getAxis() != side.getAxis()) {
                        prefferedSide = null;
                        break;
                    } else {
                        prefferedSide = side;
                    }
            }
        }
        return prefferedSide;
    }

    @Override
    public InteractionResult onWrenched(BlockState state, UseOnContext context) {
        if (context.getClickedFace()
                .getAxis()
                .isVertical()) {
            BlockEntity be = context.getLevel()
                    .getBlockEntity(context.getClickedPos());
            if (be instanceof CompressorFrameBlockEntity frame) {
                CompressorConnectivityHandler.splitMulti(frame);
                frame.removeController(true);
            }
            state = state.setValue(END, true);
            state = state.setValue(START, true);
            state = state.setValue(SHAPE, Shape.CONTROLLER);
        }
        InteractionResult onWrenched = super.onWrenched(state, context);
        return onWrenched;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        BlockState newState = state.setValue(HORIZONTAL_FACING, rot.rotate(state.getValue(HORIZONTAL_FACING)));

        return newState;
    }

    // TODO Compressor frame Voxelshape
//    @Override
//    public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
//                               CollisionContext p_220053_4_) {
//        return AllShapes.CUCKOO_CLOCK;
//    }

    public enum Shape implements StringRepresentable {
        CONTROLLER, DRIVE;

        @Override @NotNull
        public String getSerializedName() {
            return Lang.asId(name());
        }
    }

    public static boolean isFrame(BlockState state) {
        return state.getBlock() instanceof CompressorFrameBlock;
    }

    public static void updateCompressorState(BlockState pState, Level pLevel, BlockPos pPos, boolean placed) {
        BlockState tankState = pLevel.getBlockState(pPos);
        if (!(tankState.getBlock() instanceof CompressorFrameBlock frame))
            return;
        CompressorFrameBlockEntity frameBE = frame.getBlockEntity(pLevel, pPos);
        if (frameBE == null)
            return;
        CompressorFrameBlockEntity controllerBE = frameBE.getControllerBE();
        if (controllerBE == null)
            return;
        controllerBE.updateCompressorState();
    };

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction preferred = getPreferredHorizontalFacing(context);
        if (preferred != null)
            return defaultBlockState().setValue(HORIZONTAL_FACING, preferred.getOpposite());
        return this.defaultBlockState().setValue(HORIZONTAL_FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        BlockPos position = pos.relative(face);
        BlockEntity entity = getBlockEntity(world, position);
        Direction facing =  state.getValue(HORIZONTAL_FACING);
        if (entity instanceof CompressorFrameBlockEntity frameBlockEntity) {
            boolean end = state.getValue(END);
            if (!end) return face.getAxis() == facing.getAxis();
        }
        return face == facing.getOpposite();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    @Override
    public Class<CompressorFrameBlockEntity> getBlockEntityClass() {
        return CompressorFrameBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompressorFrameBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_FRAME.get();
    }
}
