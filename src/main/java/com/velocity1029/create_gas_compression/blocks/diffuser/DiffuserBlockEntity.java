package com.velocity1029.create_gas_compression.blocks.diffuser;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.api.equipment.goggles.IHaveHoveringInformation;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.velocity1029.create_gas_compression.base.FluidTransformer;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class DiffuserBlockEntity extends IronPipeBlockEntity implements IHaveGoggleInformation, IHaveHoveringInformation {

    public DiffuserBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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

    private FluidStack diffuseFluid(FluidStack fluid) {
        if (!fluid.hasTag() || fluid.isEmpty()) return fluid;
        CompoundTag tags = fluid.getTag();

        int maximumPipeDiffusion = FluidPropagator.getPipeConnections(level.getBlockState(getBlockPos()), FluidPropagator.getPipe(level, getBlockPos())).size() - 1;
        float pressure = tags.contains("Pressure", Tag.TAG_FLOAT) ? tags.getFloat("Pressure") : 1;

        float diffusionRatio = Math.min(maximumPipeDiffusion, pressure);
        float diffusedPressure = pressure / diffusionRatio;
        int diffusedAmount = (int) Math.floor(fluid.getAmount() * diffusionRatio);

        fluid.setAmount(diffusedAmount);
        if (diffusedPressure <= 1)
            fluid.setTag(null);
        else {
            tags.putFloat("Pressure", diffusedPressure);
            if (diffusionRatio >= 2)
                tags.putBoolean("Hot", false);
            fluid.setTag(tags);
        }

        return fluid;
    }

    class DiffuserFluidTransportBehaviour extends PressurizedPipeFluidTransportBehaviour implements FluidTransformer {

        public DiffuserFluidTransportBehaviour(SmartBlockEntity be) {
            super(be);
        }

        @Override
        public FluidStack transformFluid(FluidStack fluid) {
            diffuseFluid(fluid);
            return fluid;
        }

        @Override
        public FluidStack getProvidedOutwardFluid(Direction side) {
            FluidStack superFluid = super.getProvidedOutwardFluid(side);
            return diffuseFluid(superFluid.copy());
        }
    }
}
