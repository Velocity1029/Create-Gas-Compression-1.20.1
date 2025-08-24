package com.velocity1029.create_gas_compression.blocks.diffuser;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlock;
import com.simibubi.create.content.fluids.pipes.FluidPipeBlockEntity;
import com.simibubi.create.content.fluids.pipes.GlassFluidPipeBlock;
import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.IFannable;
import com.velocity1029.create_gas_compression.base.PressurizedFluidTransportBehaviour;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class DiffuserBlockEntity extends IronPipeBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {

    // Fluid Handling
    private final DiffusingTank tank;

    public DiffuserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tank = new DiffusingTank();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new DiffuserFluidTransportBehaviour(this));

        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        return containedFluidTooltip(tooltip, isPlayerSneaking,
                getCapability(ForgeCapabilities.FLUID_HANDLER));
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> tank).cast();
        return super.getCapability(cap, side);
    }

    private FluidStack diffuseFluid(FluidStack fluid) {
        if (!fluid.hasTag() || fluid.isEmpty()) return fluid;
        CompoundTag tags = fluid.getTag();

        int maximumPipeDiffusion = FluidPropagator.getPipeConnections(level.getBlockState(getBlockPos()), FluidPropagator.getPipe(level, getBlockPos())).size() - 1;
        float pressure = tags.contains("Pressure", Tag.TAG_FLOAT) ? tags.getFloat("Pressure") : 1;

        float diffusionRatio = Math.min(maximumPipeDiffusion, pressure);
        float diffusedPressure = pressure / diffusionRatio;
        int diffusedAmount = (int) Math.floor(fluid.getAmount() * diffusionRatio);

        FluidStack diffusedFluid = new FluidStack(fluid, diffusedAmount);
        tags.putFloat("Pressure", diffusedPressure);
        diffusedFluid.setTag(tags);

        return diffusedFluid;
    }

    private class DiffusingTank extends FluidTank {

        public DiffusingTank() {
            super(1000);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            if (!level.isClientSide) {
                setChanged();
                sendData();
            }
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            resource = diffuseFluid(resource);
            if (!resource.isEmpty() && this.isFluidValid(resource)) {
                if (action.simulate()) {
                    if (this.fluid.isEmpty()) {
                        return Math.min(this.capacity, resource.getAmount());
                    } else {
                        return this.fluid.getFluid() != resource.getFluid() ? 0 : Math.min(this.capacity - this.fluid.getAmount(), resource.getAmount());
                    }
                } else if (this.fluid.isEmpty()) {
                    this.fluid = new FluidStack(resource, Math.min(this.capacity, resource.getAmount()));
                    this.onContentsChanged();
                    return this.fluid.getAmount();
                } else if (this.fluid.getFluid() != resource.getFluid()) {
                    return 0;
                } else {
                    int filled = this.capacity - this.fluid.getAmount();
                    if (resource.getAmount() < filled) {
                        this.fluid.grow(resource.getAmount());
                        filled = resource.getAmount();
                    } else {
                        this.fluid.setAmount(this.capacity);
                    }

                    if (filled > 0) {
                        this.onContentsChanged();
                    }

                    return filled;
                }
            } else {
                return 0;
            }
        }
    }

    class DiffuserFluidTransportBehaviour extends PressurizedFluidTransportBehaviour {

        public DiffuserFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return diffuseFluid(superFluid);
        }

        @Override
        public boolean canHaveFlowToward(BlockState state, Direction direction) {
            return (FluidPipeBlock.isPipe(state) || state.getBlock() instanceof EncasedPipeBlock)
                    && state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction));
        }

        @Override
        public AttachmentTypes getRenderedRimAttachment(BlockAndTintGetter world, BlockPos pos, BlockState state,
                                                        Direction direction) {
            AttachmentTypes attachment = super.getRenderedRimAttachment(world, pos, state, direction);

            BlockPos offsetPos = pos.relative(direction);
            BlockState otherState = world.getBlockState(offsetPos);

            if (state.getBlock() instanceof EncasedPipeBlock && attachment != AttachmentTypes.DRAIN)
                return AttachmentTypes.NONE;

            if (attachment == AttachmentTypes.RIM) {
                if (!FluidPipeBlock.isPipe(otherState) && !(otherState.getBlock() instanceof EncasedPipeBlock)
                        && !(otherState.getBlock() instanceof GlassFluidPipeBlock)) {
                    FluidTransportBehaviour pipeBehaviour =
                            BlockEntityBehaviour.get(world, offsetPos, FluidTransportBehaviour.TYPE);
                    if (pipeBehaviour != null && pipeBehaviour.canHaveFlowToward(otherState, direction.getOpposite()))
                        return AttachmentTypes.DETAILED_CONNECTION;
                }

                if (!FluidPipeBlock.shouldDrawRim(world, pos, state, direction))
                    return FluidPropagator.getStraightPipeAxis(state) == direction.getAxis()
                            ? AttachmentTypes.CONNECTION
                            : AttachmentTypes.DETAILED_CONNECTION;
            }

            if (attachment == AttachmentTypes.NONE
                    && state.getValue(FluidPipeBlock.PROPERTY_BY_DIRECTION.get(direction)))
                return AttachmentTypes.DETAILED_CONNECTION;

            return attachment;
        }

    }
}
