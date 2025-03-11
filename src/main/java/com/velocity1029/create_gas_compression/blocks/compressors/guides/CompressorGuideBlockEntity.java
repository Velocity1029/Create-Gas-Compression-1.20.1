package com.velocity1029.create_gas_compression.blocks.compressors.guides;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
import com.simibubi.create.content.fluids.pump.PumpBlock;
import com.simibubi.create.content.kinetics.steamEngine.SteamEngineValueBox;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollOptionBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

public class CompressorGuideBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    public WeakReference<CompressorCylinderBlockEntity> target;
    public WeakReference<CompressorFrameBlockEntity> source;
    public Optional<DyeColor> color;

    float prevAngle = 0;

    public CompressorGuideBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        target = new WeakReference<>(null);
        source = new WeakReference<>(null);
        color = Optional.empty();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
//                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new SteamEngineValueBox());
//        movementDirection.onlyActiveWhen(() -> {
//            CompressorCylinderBlockEntity cylinder = getCylinder();
//            return cylinder == null || !cylinder.hasSource();
//        });
//        movementDirection.withCallback($ -> onDirectionChanged());
//        behaviours.add(movementDirection);

//        registerAwardables(behaviours, CGCAdvancements.COMPRESSOR);
    }
    // TODO onDirectionChanged??
    private void onDirectionChanged() {
    }

    @Override
    public void tick() {
        super.tick();
        CompressorFrameBlockEntity frame = getFrame();
        CompressorCylinderBlockEntity cylinder = getCylinder();

        if (frame == null || cylinder == null || !isValid()) {
            if (level.isClientSide())
                return;
            if (cylinder == null)
                return;
            if (!cylinder.getBlockPos()
                    .subtract(worldPosition)
                    .equals(cylinder.guidePos))
                return;
            if (cylinder.compressorEfficiency == 0)
                return;
            Direction facing = CompressorGuideBlock.getFacing(getBlockState());
            if (level.isLoaded(worldPosition.relative(facing.getOpposite())))
                cylinder.update(worldPosition, 0);
            return;
        }

        boolean verticalTarget = false;
        BlockState cylinderState = cylinder.getBlockState();
        Axis targetAxis = Axis.X;
        if (cylinderState.getBlock() instanceof PumpBlock ir)
            targetAxis = ir.getRotationAxis(cylinderState);
        verticalTarget = targetAxis == Axis.Y;

        BlockState blockState = getBlockState();
        if (!CGCBlocks.COMPRESSOR_GUIDE.has(blockState))
            return;
//        Direction facing = CompressorCylinderBlock.getFacing(blockState);
//        if (facing.getAxis() == Axis.Y)
//            facing = blockState.getValue(CompressorGuideBlock.FACING);


        float efficiency = Mth.clamp(frame.compressor.getCompressorEfficiency(), 0, 1);
        if (efficiency > 0)

//            award(CGCAdvancements.COMPRESSOR);

//        int conveyedSpeedLevel =
//                efficiency == 0 ? 1 : verticalTarget ? 1 : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);

        cylinder.update(worldPosition, efficiency);
    }

    @Override
    public void remove() {
        CompressorCylinderBlockEntity cylinder = getCylinder();
        if (cylinder != null)
            cylinder.remove(worldPosition);
        super.remove();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(2);
    }

    public CompressorCylinderBlockEntity getCylinder() {
        if (target == null) return null;
        CompressorCylinderBlockEntity cylinder = target.get();
        if (cylinder == null || cylinder.isRemoved() || !cylinder.canBePoweredBy(worldPosition)) {
            if (cylinder != null)
                target = new WeakReference<>(null);
            Direction facing = CompressorGuideBlock.getFacing(getBlockState());
            BlockEntity anyCylinderAt = level.getBlockEntity(worldPosition.relative(facing, 1));
            if (anyCylinderAt instanceof CompressorCylinderBlockEntity cc && cc.canBePoweredBy(worldPosition))
                target = new WeakReference<>(cylinder = cc);
        }
        return cylinder;
    }

    public CompressorFrameBlockEntity getFrame() {
        CompressorFrameBlockEntity frame = source.get();
        if (frame == null || frame.isRemoved()) {
            if (frame != null)
                source = new WeakReference<>(null);
            Direction facing = CompressorGuideBlock.getFacing(getBlockState());
            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
            if (be instanceof CompressorFrameBlockEntity frameBe)
                source = new WeakReference<>(frame = frameBe);
        }
        if (frame == null)
            return null;
//        return frame.getControllerBE();
        return null; //TODO is this ok?
    }

    public boolean isValid() {
        Direction dir = CompressorGuideBlock.getConnectedDirection(getBlockState()).getOpposite();

        Level level = getLevel();
        if (level == null)
            return false;

        return level.getBlockState(getBlockPos().relative(dir)).is(CGCBlocks.COMPRESSOR_CYLINDER.get());
    }

    public boolean applyColor(DyeColor colorIn) {
        if (colorIn == null) {
            if (!color.isPresent())
                return false;
        } else if (color.isPresent() && color.get() == colorIn)
            return false;
        if (level.isClientSide())
            return true;

//        for (BlockPos blockPos : BeltBlock.getBeltChain(level, getController())) {
//            BeltBlockEntity belt = BeltHelper.getSegmentBE(level, blockPos);
//            if (belt == null)
//                continue;

        color = Optional.ofNullable(colorIn);
        setChanged();
        sendData();
//        }

        return true;
    }

//    public boolean isValid() {
//        BlockPos framePos = getFrame().getBlockPos();
//
//        Direction direction = switch (getBlockState().getValue(CompressorGuideBlockEntity.FACE)) {
//            case FLOOR -> Direction.DOWN;
//            case WALL -> getBlockState().getValue(CompressorFrameBlock.FACING);
//            case CEILING -> Direction.UP;
//        };
//
//        return getBlockPos().relative(direction).equals(tankPos);
//    }
}
