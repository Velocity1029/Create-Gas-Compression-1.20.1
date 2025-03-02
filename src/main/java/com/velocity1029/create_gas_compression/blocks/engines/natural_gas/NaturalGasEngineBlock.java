package com.velocity1029.create_gas_compression.blocks.engines.natural_gas;

import java.util.Objects;
import java.util.function.Predicate;

//import com.drmangotea.tfmg.registry.TFMGBlockEntities;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.simibubi.create.content.fluids.tank.FluidTankBlock;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.content.kinetics.steamEngine.PoweredShaftBlock;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.placement.PlacementHelpers;
import com.simibubi.create.foundation.placement.PlacementOffset;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.Couple;
//import com.tterrag.registrate.util.entry.BlockEntry;
import java.util.Objects;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class NaturalGasEngineBlock extends HorizontalDirectionalBlock implements IWrenchable, IBE<NaturalGasEngineBlockEntity> {
    public static final EnumProperty<AttachFace> FACE;
    private static final int placementHelperId;

    public NaturalGasEngineBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.m_49959_((BlockState)((BlockState)((BlockState)((BlockState)this.f_49792_.m_61090_()).m_61124_(FACE, AttachFace.FLOOR)).m_61124_(f_54117_, Direction.NORTH)).m_61124_(BlockStateProperties.f_61362_, false));
    }

    public static Direction getConnectedDirection(BlockState p_53201_) {
        switch ((AttachFace)p_53201_.m_61143_(FACE)) {
            case CEILING -> return Direction.DOWN;
            case FLOOR -> return Direction.UP;
            default -> return (Direction)p_53201_.m_61143_(f_54117_);
        }
    }

    protected void m_7926_(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.m_7926_(pBuilder.m_61104_(new Property[]{FACE, f_54117_, BlockStateProperties.f_61362_}));
    }

    @Nullable
    public BlockState m_5573_(BlockPlaceContext p_53184_) {
        Direction[] var2 = p_53184_.m_6232_();
        int var3 = var2.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            Direction direction = var2[var4];
            BlockState blockstate;
            if (direction.m_122434_() == Axis.Y) {
                blockstate = (BlockState)((BlockState)this.m_49966_().m_61124_(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)).m_61124_(f_54117_, p_53184_.m_8125_());
            } else {
                blockstate = (BlockState)((BlockState)this.m_49966_().m_61124_(FACE, AttachFace.WALL)).m_61124_(f_54117_, direction.m_122424_());
            }

            if (blockstate.m_60710_(p_53184_.m_43725_(), p_53184_.m_8083_())) {
                return blockstate;
            }
        }

        return null;
    }

    public InteractionResult m_6227_(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult ray) {
        ItemStack heldItem = player.m_21120_(hand);
        IPlacementHelper placementHelper = PlacementHelpers.get(placementHelperId);
        return placementHelper.matchesItem(heldItem) ? placementHelper.getOffset(player, world, state, pos, ray).placeInWorld(world, (BlockItem)heldItem.m_41720_(), player, hand, ray) : InteractionResult.PASS;
    }

    public BlockState m_7417_(BlockState state, Direction direction, BlockState neighbourState, LevelAccessor world, BlockPos pos, BlockPos neighbourPos) {
        return state;
    }

    public void m_6807_(BlockState pState, Level pLevel, BlockPos pPos, BlockState pOldState, boolean pIsMoving) {
        BlockPos shaftPos = getShaftPos(pState, pPos);
        BlockState shaftState = pLevel.m_8055_(shaftPos);
        if (isShaftValid(pState, shaftState)) {
            pLevel.m_7731_(shaftPos, PoweredShaftBlock.getEquivalent(shaftState), 3);
        }

    }

    public void m_6810_(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.m_155947_() && (!pState.m_60713_(pNewState.m_60734_()) || !pNewState.m_155947_())) {
            pLevel.m_46747_(pPos);
        }

        FluidTankBlock.updateBoilerState(pState, pLevel, pPos.m_121945_(getFacing(pState).m_122424_()));
        BlockPos shaftPos = getShaftPos(pState, pPos);
        BlockState shaftState = pLevel.m_8055_(shaftPos);
        if (AllBlocks.POWERED_SHAFT.has(shaftState)) {
            pLevel.m_186460_(shaftPos, shaftState.m_60734_(), 1);
        }

    }

    public VoxelShape m_5940_(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        AttachFace face = (AttachFace)pState.m_61143_(FACE);
        Direction direction = (Direction)pState.m_61143_(f_54117_);
        return face == AttachFace.CEILING ? AllShapes.STEAM_ENGINE_CEILING.get(direction.m_122434_()) : (face == AttachFace.FLOOR ? AllShapes.STEAM_ENGINE.get(direction.m_122434_()) : AllShapes.STEAM_ENGINE_WALL.get(direction));
    }

    public boolean m_7357_(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
        return false;
    }

    public static Direction getFacing(BlockState sideState) {
        return getConnectedDirection(sideState);
    }

    public static BlockPos getShaftPos(BlockState sideState, BlockPos pos) {
        return pos.m_5484_(getConnectedDirection(sideState), 2);
    }

    public static boolean isShaftValid(BlockState state, BlockState shaft) {
        return (AllBlocks.SHAFT.has(shaft) || AllBlocks.POWERED_SHAFT.has(shaft)) && shaft.m_61143_(ShaftBlock.AXIS) != getFacing(state).m_122434_();
    }

    public Class<DieselEngineBlockEntity> getBlockEntityClass() {
        return DieselEngineBlockEntity.class;
    }

    public BlockEntityType<? extends DieselEngineBlockEntity> getBlockEntityType() {
        return (BlockEntityType)TFMGBlockEntities.DIESEL_ENGINE.get();
    }

    public static Couple<Integer> getSpeedRange() {
        return Couple.create(16, 128);
    }

    static {
        FACE = BlockStateProperties.f_61376_;
        placementHelperId = PlacementHelpers.register(new PlacementHelper());
    }

    @MethodsReturnNonnullByDefault
    private static class PlacementHelper implements IPlacementHelper {
        private PlacementHelper() {
        }

        public Predicate<ItemStack> getItemPredicate() {
            BlockEntry var10000 = AllBlocks.SHAFT;
            Objects.requireNonNull(var10000);
            return var10000::isIn;
        }

        public Predicate<BlockState> getStatePredicate() {
            return (s) -> {
                return s.m_60734_() instanceof DieselEngineBlock;
            };
        }

        public PlacementOffset getOffset(Player player, Level world, BlockState state, BlockPos pos, BlockHitResult ray) {
            BlockPos shaftPos = DieselEngineBlock.getShaftPos(state, pos);
            BlockState shaft = AllBlocks.SHAFT.getDefaultState();
            Direction[] var8 = Direction.m_122382_(player);
            int var9 = var8.length;

            for(int var10 = 0; var10 < var9; ++var10) {
                Direction direction = var8[var10];
                shaft = (BlockState)shaft.m_61124_(ShaftBlock.AXIS, direction.m_122434_());
                if (DieselEngineBlock.isShaftValid(state, shaft)) {
                    break;
                }
            }

            BlockState newState = world.m_8055_(shaftPos);
            if (!newState.m_247087_()) {
                return PlacementOffset.fail();
            } else {
                Direction.Axis axis = (Direction.Axis)shaft.m_61143_(ShaftBlock.AXIS);
                return PlacementOffset.success(shaftPos, (s) -> {
                    return (BlockState)BlockHelper.copyProperties(s, AllBlocks.POWERED_SHAFT.getDefaultState()).m_61124_(PoweredShaftBlock.AXIS, axis);
                });
            }
        }
    }
}

