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
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlockEntity;
import com.velocity1029.create_gas_compression.registry.CGCTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
        behaviours.add(new StandardPipeFluidTransportBehaviour(this));

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
        tank.coolFluid(tank.getFluid());
    }

    private static class CoolingTank extends FluidTank {
        public boolean shouldCool;

        public CoolingTank() {
            super(0);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (!resource.isEmpty() && this.isFluidValid(resource)) {
                if (action.simulate()) {
                    if (this.fluid.isEmpty() || this.fluid.getAmount() == 0) {
                        return resource.getAmount();
                    } else {
                        return 0;
                    }
                } else if (this.fluid.isEmpty() || this.fluid.getAmount() == 0) {
                    this.fluid = shouldCool ? coolFluid(resource.copy()) : resource;
                    this.onContentsChanged();
                    return this.fluid.getAmount();
                } else {
                    return 0;
                }
            } else {
                return 0;
            }
        }

        protected FluidStack coolFluid(FluidStack fluid) {
            if (!fluid.hasTag()) return fluid;
            CompoundTag tags = fluid.getTag();
            boolean hot = tags.contains("Hot") && tags.getBoolean("Hot");
            tags.putBoolean("Hot", false);
            return fluid;
        }
    }

    class StandardPipeFluidTransportBehaviour extends FluidTransportBehaviour {

        public StandardPipeFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
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
