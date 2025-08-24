package com.velocity1029.create_gas_compression.blocks.heat_exchanger;

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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

public class CompressedGasCoolerBlockEntity extends FluidPipeBlockEntity implements IFannable {

    // Fluid Handling
    private final CoolingTank tank;
    private int coolCounter = 0;

    public CompressedGasCoolerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        tank = new CoolingTank();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        behaviours.add(new CoolerFluidTransportBehaviour(this));

        registerAwardables(behaviours, FluidPropagator.getSharedTriggers());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER)
            return LazyOptional.of(() -> tank).cast();
        return super.getCapability(cap, side);
    }

    @Override
    public void tick() {
        super.tick();
        if (tank.shouldCool) {
            coolCounter--;
            if (coolCounter <= 0) {
                tank.shouldCool = false;
            }
        }
    }

    @Override
    public void fan(AirCurrent airCurrent) {
        float speed = airCurrent.source.getSpeed();
        tank.shouldCool = true;
        coolCounter = 5;
        coolFluid(tank.getFluid());
    }

    protected static FluidStack coolFluid(FluidStack fluid) {
        if (fluid.isEmpty() || !fluid.hasTag()) return fluid;
        CompoundTag tags = fluid.getTag();
        boolean hot = tags.contains("Hot") && tags.getBoolean("Hot");
        tags.putBoolean("Hot", false);
        return fluid;
    }

    private static class CoolingTank extends FluidTank {
        public boolean shouldCool;

        public CoolingTank() {
            super(1000);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.isEmpty() && this.isFluidValid(resource)) {
                if (action.simulate()) {
                    if (this.fluid.isEmpty()) {
                        return Math.min(this.capacity, resource.getAmount());
                    } else {
                        return this.fluid.getFluid() != resource.getFluid() ? 0 : Math.min(this.capacity - this.fluid.getAmount(), resource.getAmount());
                    }
                } else if (this.fluid.isEmpty()) {
                    this.fluid = coolFluid(new FluidStack(resource, Math.min(this.capacity, resource.getAmount())));
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

    class CoolerFluidTransportBehaviour extends FluidTransportBehaviour {

        public CoolerFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return coolFluid(superFluid);
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
