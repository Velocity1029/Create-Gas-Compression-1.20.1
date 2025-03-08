package com.velocity1029.create_gas_compression.blocks.compressors.guides;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.contraptions.bearing.WindmillBearingBlockEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.lang.ref.WeakReference;
import java.util.List;

public class CompressorGuideBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {

    protected ScrollOptionBehaviour<WindmillBearingBlockEntity.RotationDirection> movementDirection;

    public WeakReference<CompressorCylinderBlockEntity> target;
    public WeakReference<CompressorFrameBlockEntity> source;

    float prevAngle = 0;

    public CompressorGuideBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        movementDirection = new ScrollOptionBehaviour<>(WindmillBearingBlockEntity.RotationDirection.class,
                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new SteamEngineValueBox());
        movementDirection.onlyActiveWhen(() -> {
            CompressorCylinderBlockEntity cylinder = getCylinder();
            return cylinder == null || !cylinder.hasSource();
        });
        movementDirection.withCallback($ -> onDirectionChanged());
        behaviours.add(movementDirection);

//        registerAwardables(behaviours, AllAdvancements.STEAM_ENGINE);
    }
    // TODO onDirectionChanged??
    private void onDirectionChanged() {
    }

    @Override
    public void tick() {
        super.tick();
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
