package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.IMultiBlockEntityContainer;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.IMultiBlockEntityCompressor;
import com.velocity1029.create_gas_compression.blocks.compressors.CompressorConnectivityHandler;
import com.velocity1029.create_gas_compression.blocks.compressors.CompressorData;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public class CompressorFrameBlockEntity extends KineticBlockEntity implements IHaveGoggleInformation, IMultiBlockEntityContainer {

    private static final int MAX_SIZE = 3;

//    protected LazyOptional<IFluidHandler> fluidCapability;
    protected boolean forceFluidLevelUpdate;
//    protected FluidTank tankInventory;
    protected BlockPos controller;
    protected BlockPos lastKnownPos;
    protected boolean updateConnectivity;
    protected boolean updateCapability;
//    protected boolean window;
//    protected int luminosity;
//    protected Direction direction;
    protected int length;

    public CompressorData compressor;

    private static final int SYNC_RATE = 8;
    protected int syncCooldown;
    protected boolean queuedSync;

    public CompressorFrameBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        updateConnectivity = false;
//        direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        length = 1;
        compressor = new CompressorData();
    }

    public Direction getDirection() {
        return getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
    }

    public void setCompressorRPM() {
        compressor.RPM = speed;
    }

//    @Override
//    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
//
//    }

    protected void updateConnectivity() {
        updateConnectivity = false;
        if (level.isClientSide())
            return;
        if (!isController())
            return;
//        if (level.getBlockEntity(worldPosition.relative(direction)) instanceof CompressorFrameBlockEntity neighbourFrameEntity) {
//            if (neighbourFrameEntity.direction == direction)
//                ConnectivityHandler.formMulti(this);
//        }
//        if (level.getBlockEntity(worldPosition.relative(direction.getOpposite())) instanceof CompressorFrameBlockEntity neighbourFrameEntity) {
//            if (neighbourFrameEntity.direction == direction)
//                ConnectivityHandler.formMulti(this);
//        }
        CompressorConnectivityHandler.formMulti(this);
    }

    @Override
    public void tick() {
        super.tick();
        if (syncCooldown > 0) {
            syncCooldown--;
            if (syncCooldown == 0 && queuedSync)
                sendData();
        }

        if (lastKnownPos == null)
            lastKnownPos = getBlockPos();
        else if (!lastKnownPos.equals(worldPosition) && worldPosition != null) {
            onPositionChanged();
            return;
        }

//        if (updateCapability) {
//            updateCapability = false;
//            refreshCapability();
//        }
        if (updateConnectivity)
            updateConnectivity();
//        if (fluidLevel != null)
//            fluidLevel.tickChaser();
        if (isController())
            compressor.tick(this);
    }

//    @Override
//    public void lazyTick() {
//        super.lazyTick();
//        if (isController())
//            compressor.updateOcclusion(this);
//    }

    public void updateCompressorState() {
        if (!isController())
            return;

//        boolean wasCompressor = compressor.isActive();
        boolean changed = compressor.evaluate(this);
//
//        if (wasCompressor != compressor.isActive()) {
////            if (compressor.isActive())
////                setWindows(false);
//
//            for (int offset = 0; offset < length; offset++)
//                        if (level.getBlockEntity(
//                                worldPosition.relative(direction, offset)) instanceof CompressorFrameBlockEntity fbe)
//                            fbe.refreshCapability();
//        }

        if (changed) {
            notifyUpdate();
//            compressor.checkPipeOrganAdvancement(this);
        }
    }

    @Override
    public BlockPos getController() {
        return isController() ? worldPosition : controller;
    }

    @SuppressWarnings("unchecked")
    @Override
    public CompressorFrameBlockEntity getControllerBE() {
        if (isController())
            return this;
        BlockEntity blockEntity = level.getBlockEntity(controller);
        if (blockEntity instanceof CompressorFrameBlockEntity)
            return (CompressorFrameBlockEntity) blockEntity;
        return null;
    }

    @Override
    public boolean isController() {
        return controller == null || worldPosition.getX() == controller.getX()
                && worldPosition.getY() == controller.getY() && worldPosition.getZ() == controller.getZ();
    }

    @Override
    public void initialize() {
        super.initialize();
        sendData();
        if (level.isClientSide)
            invalidateRenderBoundingBox();
    }

    private void onPositionChanged() {
        removeController(true);
        lastKnownPos = worldPosition;
    }

    @Override
    public void setController(BlockPos controller) {
        if (level.isClientSide && !isVirtual())
            return;
        if (controller.equals(this.controller))
            return;
        this.controller = controller;
//        refreshCapability();//itemCapability.invalidate();
        setChanged();
        sendData();
    }

    @Override
    public void removeController(boolean keepContents) {
        if (level.isClientSide())
            return;
        updateConnectivity = true;
        controller = null;
        length = 1;
        compressor.clear();

        BlockState state = getBlockState();
        if (CompressorFrameBlock.isFrame(state)) {
            state = state.setValue(CompressorFrameBlock.START, true);
            state = state.setValue(CompressorFrameBlock.END, true);
            state = state.setValue(CompressorFrameBlock.SHAPE, CompressorFrameBlock.Shape.CONTROLLER);
            getLevel().setBlock(worldPosition, state, 22);
        }

//        itemCapability.invalidate();
        setChanged();
        sendData();
    }

    @Override
    public BlockPos getLastKnownPos() {
        return lastKnownPos;
    }

    @Override
    public void preventConnectivityUpdate() {
        updateConnectivity = false;
    }

    @Override
    public void notifyMultiUpdated() {
        BlockState state = this.getBlockState();
        if (CompressorFrameBlock.isFrame(state)) { // safety
            BlockPos controller = getController();
            BlockPos startPos = controller.relative(getDirection(), length - 1);
            state = state.setValue(CompressorFrameBlock.START, startPos.equals(worldPosition));
            state = state.setValue(CompressorFrameBlock.END, isController());
            level.setBlock(getBlockPos(), state, 6);
        }
//        if (isController())
//            setWindows(window);
//        onFluidStackChanged(tankInventory.getFluid());
        updateCompressorState();
        setChanged();
    }

    @Override
    protected void read(CompoundTag compound, boolean clientPacket) {
        super.read(compound, clientPacket);

        BlockPos controllerBefore = controller;
        int prevSize = length;

        updateConnectivity = compound.contains("Uninitialized");
        controller = null;
        lastKnownPos = null;

        if (compound.contains("LastKnownPos"))
            lastKnownPos = NbtUtils.readBlockPos(compound.getCompound("LastKnownPos"));
        if (compound.contains("Controller"))
            controller = NbtUtils.readBlockPos(compound.getCompound("Controller"));

        if (isController()) {
            length = compound.getInt("Size");
        }

        compressor.read(compound.getCompound("Compressor"), length);

//        updateCapability = true;

        if (!clientPacket)
            return;

        boolean changeOfController = !Objects.equals(controllerBefore, controller);
        if (changeOfController || prevSize != length) {
            if (hasLevel())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 16);
//            if (isController())
//                tankInventory.setCapacity(getCapacityMultiplier() * getTotalTankSize());
            invalidateRenderBoundingBox();
        }
//        if (luminosity != prevLum && hasLevel())
//            level.getChunkSource()
//                    .getLightEngine()
//                    .checkBlock(worldPosition);

//        if (compound.contains("LazySync"))
//            fluidLevel.chase(fluidLevel.getChaseTarget(), 0.125f, Chaser.EXP);
    }

    @Override
    public void write(CompoundTag compound, boolean clientPacket) {
        if (updateConnectivity)
            compound.putBoolean("Uninitialized", true);
        compound.put("Compressor", compressor.write());
        if (lastKnownPos != null)
            compound.put("LastKnownPos", NbtUtils.writeBlockPos(lastKnownPos));
        if (!isController())
            compound.put("Controller", NbtUtils.writeBlockPos(controller));
        if (isController()) {
//            compound.put("TankContent", tankInventory.writeToNBT(new CompoundTag()));
            compound.putInt("Size", length);
        }
        super.write(compound, clientPacket);

//        if (!clientPacket)
//            return;
//        if (queuedSync)
//            compound.putBoolean("LazySync", true);
    }

    @Override
    public Direction.Axis getMainConnectionAxis() {
        return getMainAxisOf(this);
    }

    @Override
    public int getMaxLength(Direction.Axis longAxis, int width) {
        if (longAxis == Direction.Axis.Y) return 1;
        return 3;
    }

    @Override
    public int getMaxWidth() {
        return 1;
    }

    @Override
    public int getHeight() {
        return length;
    }

    @Override
    public void setHeight(int height) {
        this.length = height;
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public void setWidth(int width) {

    }

    @Override
    public void sendData() {
        if (syncCooldown > 0) {
            queuedSync = true;
            return;
        }
        super.sendData();
        queuedSync = false;
        syncCooldown = SYNC_RATE;
    }

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
