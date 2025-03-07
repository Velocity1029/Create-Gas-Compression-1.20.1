//package com.velocity1029.create_gas_compression.blocks.compressors.guides;
//
//import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
//import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
//import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
//import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlock;
//import net.minecraft.core.BlockPos;
//import net.minecraft.core.Direction;
//import net.minecraft.world.level.block.entity.BlockEntity;
//import net.minecraft.world.phys.AABB;
//import net.minecraftforge.api.distmarker.Dist;
//import net.minecraftforge.api.distmarker.OnlyIn;
//
//import java.lang.ref.WeakReference;
//import java.util.List;
//
//public class CompressorGuideBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
//
//    @Override
//    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//        movementDirection = new ScrollOptionBehaviour<>(RotationDirection.class,
//                CreateLang.translateDirect("contraptions.windmill.rotation_direction"), this, new SteamEngineValueBox());
//        movementDirection.onlyActiveWhen(() -> {
//            PoweredShaftBlockEntity cylinder = getCylinder();
//            return cylinder == null || !cylinder.hasSource();
//        });
//        movementDirection.withCallback($ -> onDirectionChanged());
//        behaviours.add(movementDirection);
//
////        registerAwardables(behaviours, AllAdvancements.STEAM_ENGINE);
//    }
//
//    @Override
//    public void tick() {
//        super.tick();
//    }
//
//    @Override
//    public void remove() {
//        PoweredShaftBlockEntity cylinder = getCylinder();
//        if (cylinder != null)
//            cylinder.remove(worldPosition);
//        super.remove();
//    }
//
//    @Override
//    @OnlyIn(Dist.CLIENT)
//    protected AABB createRenderBoundingBox() {
//        return super.createRenderBoundingBox().inflate(2);
//    }
//
//    public PoweredShaftBlockEntity getCylinder() {
//        PoweredShaftBlockEntity cylinder = target.get();
//        if (cylinder == null || cylinder.isRemoved() || !cylinder.canBePoweredBy(worldPosition)) {
//            if (cylinder != null)
//                target = new WeakReference<>(null);
//            Direction facing = PoweredShaftBlock.getFacing(getBlockState());
//            BlockEntity anyCylinderAt = level.getBlockEntity(worldPosition.relative(facing, 1));
//            if (anyCylinderAt instanceof PoweredShaftBlockEntity ps && ps.canBePoweredBy(worldPosition))
//                target = new WeakReference<>(cylinder = ps);
//        }
//        return cylinder;
//    }
//
//    public CompressorFrameBlockEntity getFrame() {
//        CompressorFrameBlockEntity frame = source.get();
//        if (frame == null || frame.isRemoved()) {
//            if (frame != null)
//                source = new WeakReference<>(null);
//            Direction facing = CompressorGuideBlock.getFacing(getBlockState());
//            BlockEntity be = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
//            if (be instanceof CompressorFrameBlockEntity frameBe)
//                source = new WeakReference<>(frame = frameBe);
//        }
//        if (frame == null)
//            return null;
//        return frame.getControllerBE();
//    }
//
//    public boolean isValid() {
//        BlockPos framePos = getFrame().getBlockPos();
//
//        Direction direction = switch (getBlockState().getValue(CompressorGuideBlockEntity.FACE)) {
//            case FLOOR -> Direction.DOWN;
//            case WALL -> getBlockState().getValue(SteamEngineBlock.FACING);
//            case CEILING -> Direction.UP;
//        };
//
//        return getBlockPos().relative(direction).equals(tankPos);
//    }
//}
