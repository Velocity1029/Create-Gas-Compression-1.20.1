package com.velocity1029.create_gas_compression.blocks.compressors.guides;

import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.foundation.advancement.AdvancementBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.BlockHelper;

import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.registry.CGCBlockEntities;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.createmod.catnip.data.Couple;
import net.createmod.catnip.placement.IPlacementHelper;
import net.createmod.catnip.placement.PlacementHelpers;
import net.createmod.catnip.placement.PlacementOffset;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.Tags;

import java.util.function.Predicate;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class CompressorGuideBlock extends FaceAttachedHorizontalDirectionalBlock
        implements IRotate, SimpleWaterloggedBlock, IWrenchable, IBE<CompressorGuideBlockEntity> {

    private static final int placementHelperId = PlacementHelpers.register(new PlacementHelper());

    public CompressorGuideBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false));
    }

    @Override
    protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACE, FACING, WATERLOGGED));
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, LivingEntity pPlacer, ItemStack pStack) {
        super.setPlacedBy(pLevel, pPos, pState, pPlacer, pStack);
        AdvancementBehaviour.setPlacedBy(pLevel, pPos, pPlacer);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return canAttach(pLevel, pPos, getConnectedDirection(pState).getOpposite());
    }

    public static boolean canAttach(LevelReader pReader, BlockPos pPos, Direction pDirection) {
        BlockPos blockpos = pPos.relative(pDirection);
        BlockState blockState = pReader.getBlockState(blockpos);
        BlockEntity blockEntity = pReader.getBlockEntity(blockpos);
        if (blockEntity instanceof CompressorFrameBlockEntity frameEntity) {
            Axis facing = blockState.getValue(HORIZONTAL_FACING).getAxis();
            Axis placement = pDirection.getAxis();
            return (placement.isHorizontal() && facing != placement);
        }
        return false;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult ray) {
        ItemStack heldItem = player.getItemInHand(hand);

        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        if (placementHelper.matchesItem(heldItem))
            return placementHelper.getOffset(player, world, state, pos, ray)
                    .placeInWorld(world, (BlockItem) heldItem.getItem(), player, hand, ray);


        boolean isDye = heldItem.is(Tags.Items.DYES);
        boolean hasWater = GenericItemEmptying.emptyItem(world, heldItem, true)
                .getFirst()
                .getFluid()
                .isSame(Fluids.WATER);
        if (isDye || hasWater)
            return onBlockEntityUse(world, pos,
                    be -> be.applyColor(DyeColor.getColor(heldItem)) ? InteractionResult.SUCCESS : InteractionResult.PASS);
        return InteractionResult.PASS;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world,
                                  BlockPos pos, BlockPos neighbourPos) {
        if (state.getValue(WATERLOGGED))
            world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        return state;
    }

    @Override
    public void onPlace(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        CompressorFrameBlock.updateCompressorState(pState, pLevel, pPos.relative(getFacing(pState).getOpposite()), true);
        BlockPos cylinderPos = getCylinderPos(pState, pPos);
        BlockState cylinderState = pLevel.getBlockState(cylinderPos);
//        if (isCylinderValid(pState, cylinderState))
            //TODO make cylinder active
//            pLevel.setBlock(cylinderPos, CompressorCylinderBlock.getEquivalent(cylinderState), 3);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.hasBlockEntity() && (!pState.is(pNewState.getBlock()) || !pNewState.hasBlockEntity()))
            pLevel.removeBlockEntity(pPos);
        CompressorFrameBlock.updateCompressorState(pState, pLevel, pPos.relative(getFacing(pState).getOpposite()), false);
        BlockPos cylinderPos = getCylinderPos(pState, pPos);
        BlockState cylinderState = pLevel.getBlockState(cylinderPos);
        if (CGCBlocks.COMPRESSOR_CYLINDER.has(cylinderState))
            pLevel.scheduleTick(cylinderPos, cylinderState.getBlock(), 1);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        AttachFace face = pState.getValue(FACE);
        Direction direction = pState.getValue(FACING);
        return face == AttachFace.CEILING ? AllShapes.STEAM_ENGINE_CEILING.get(direction.getAxis())
                : face == AttachFace.FLOOR ? AllShapes.STEAM_ENGINE.get(direction.getAxis())
                : AllShapes.STEAM_ENGINE_WALL.get(direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        FluidState ifluidstate = level.getFluidState(pos);
        BlockState state = super.getStateForPlacement(context);
        if (state == null)
            return null;
        return state.setValue(WATERLOGGED, Boolean.valueOf(ifluidstate.getType() == Fluids.WATER));
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    public static Direction getFacing(BlockState sideState) {
        return getConnectedDirection(sideState);
    }

    public static BlockPos getCylinderPos(BlockState sideState, BlockPos pos) {
        return pos.relative(getConnectedDirection(sideState), 1);
    }

    public static boolean isCylinderValid(BlockState state, BlockState cylinder) {
        return (cylinder.getBlock() instanceof CompressorCylinderBlock)// || AllBlocks.POWERED_SHAFT.has(shaft))
                && cylinder.getValue(CompressorCylinderBlock.ATTACHED_FACE) == getFacing(state).getOpposite();
    }

    @Override
    public Class<CompressorGuideBlockEntity> getBlockEntityClass() {
        return CompressorGuideBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends CompressorGuideBlockEntity> getBlockEntityType() {
        return CGCBlockEntities.COMPRESSOR_GUIDE.get();
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return false;
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return null;
    }

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        @Override
        public Predicate<ItemStack> getItemPredicate() {
            return CGCBlocks.COMPRESSOR_CYLINDER::isIn;
        }

        @Override
        public Predicate<BlockState> getStatePredicate() {
            return s -> s.getBlock() instanceof CompressorGuideBlock;
        }

        @Override
        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos,
                                         BlockHitResult ray) {
            BlockPos cylinderPos = CompressorGuideBlock.getCylinderPos(state, pos);
            BlockState cylinder = CGCBlocks.COMPRESSOR_CYLINDER.getDefaultState();
            for (Direction direction : Direction.orderedByNearest(player)) {
                cylinder = cylinder.setValue(CompressorCylinderBlock.ATTACHED_FACE, direction);
                if (isCylinderValid(state, cylinder))
                    break;
            }
            for (Direction direction : Direction.orderedByNearest(player)) {
                BlockPos adjPos = pos.relative(direction);
                BlockState adjState = world.getBlockState(adjPos);
                if (!FluidPipeBlock.canConnectTo(world, adjPos, adjState, direction) ||
                        cylinder.getValue(CompressorCylinderBlock.ATTACHED_FACE).getAxis() == direction.getAxis())
                    continue;
                cylinder = cylinder.setValue(BlockStateProperties.FACING, direction);
                if (isCylinderValid(state, cylinder))
                    break;
            }
            if (cylinder.getValue(BlockStateProperties.FACING).getAxis() == cylinder.getValue(CompressorCylinderBlock.ATTACHED_FACE).getAxis())
                cylinder = cylinder.setValue(BlockStateProperties.FACING, cylinder.getValue(BlockStateProperties.FACING).getClockWise());

            BlockState newState = world.getBlockState(cylinderPos);
            if (!newState.canBeReplaced())
                return PlacementOffset.fail();

            Direction attachment = cylinder.getValue(CompressorCylinderBlock.ATTACHED_FACE);
            Direction direction = cylinder.getValue(BlockStateProperties.FACING);
            return PlacementOffset.success(cylinderPos,
                    s -> BlockHelper
                            .copyProperties(s,
                                    (CGCBlocks.COMPRESSOR_CYLINDER).getDefaultState())
                            .setValue(CompressorCylinderBlock.ATTACHED_FACE, attachment)
                            .setValue(BlockStateProperties.FACING, direction));
        }
    }

    public static Couple<Integer> getSpeedRange() {
        return Couple.create(16, 64);
    }

    public static Direction getConnectedDirection(BlockState state) {
        return FaceAttachedHorizontalDirectionalBlock.getConnectedDirection(state);
    }
}
