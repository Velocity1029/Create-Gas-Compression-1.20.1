package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.IMultiBlockEntityCompressor;
import com.velocity1029.create_gas_compression.blocks.compressors.CompressorData;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;

public class CompressorFrameBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation{//, IMultiBlockEntityCompressor {

    private static final int MAX_SIZE = 3;

//    protected LazyOptional<IFluidHandler> fluidCapability;
    protected boolean forceFluidLevelUpdate;
//    protected FluidTank tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
    protected boolean window;
    protected int luminosity;
    protected int width;
    protected int height;

    public CompressorData compressor;
    protected int size;

    public CompressorFrameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction.Axis guideAxis;

    public void setGuideAxis(Direction.Axis axis) {
        guideAxis = axis;
    }

    public Direction.Axis getGuideAxis() {
        return guideAxis;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

//    public void updateCompressorState() {
//        if (!isController())
//            return;
//
//        boolean wasBoiler = compressor.isActive();
//        boolean changed = compressor.evaluate(this);
//
//        if (wasBoiler != compressor.isActive()) {
//            if (compressor.isActive())
////                setWindows(false);
//
//            for (int yOffset = 0; yOffset < height; yOffset++)
//                for (int xOffset = 0; xOffset < width; xOffset++)
//                    for (int zOffset = 0; zOffset < width; zOffset++)
//                        if (level.getBlockEntity(
//                                worldPosition.offset(xOffset, yOffset, zOffset)) instanceof CompressorFrameBlockEntity fbe)
//                            fbe.refreshCapability();
//        }
//
//        if (changed) {
//            notifyUpdate();
////            compressor.checkPipeOrganAdvancement(this);
//        }
//    }

//    @Override
//    public void setController(BlockPos controller) {
//        if (level.isClientSide && !isVirtual())
//            return;
//        if (controller.equals(this.controller))
//            return;
//        this.controller = controller;
//        refreshCapability();
//        setChanged();
//        sendData();
//    }
//
//    private void refreshCapability() {
//        LazyOptional<IFluidHandler> oldCap = fluidCapability;
//        fluidCapability = LazyOptional.of(this::handlerForCapability);
//        oldCap.invalidate();
//    }
//
//    private IFluidHandler handlerForCapability() {
//        return isController() ? compressor.isActive() ? compressor.createHandler() : tankInventory
//                : getControllerBE() != null ? getControllerBE().handlerForCapability() : new FluidTank(0);
//    }
//
//    @Override
//    public BlockPos getController() {
//        return isController() ? worldPosition : controller;
//    }

//    @Override
//    protected AABB createRenderBoundingBox() {
//        if (isController())
//            return super.createRenderBoundingBox().expandTowards(width - 1, height - 1, width - 1);
//        else
//            return super.createRenderBoundingBox();
//    }

//    @Nullable
//    public CompressorFrameBlockEntity getOtherCompressorFrameBlockEntity(Direction direction) {
//        BlockEntity otherBE = level.getBlockEntity(worldPosition.relative(direction));
//        if (otherBE instanceof CompressorFrameBlockEntity)
//            return (CompressorFrameBlockEntity) otherBE;
//        return null;
//    }
}
