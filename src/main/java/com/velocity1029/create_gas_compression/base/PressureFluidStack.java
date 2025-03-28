package com.velocity1029.create_gas_compression.base;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class PressureFluidStack extends FluidStack {
    private float pressure = 1;

//    public PressureFluidStack(FluidStack fluidStack) {
//
//    }

    public PressureFluidStack(Fluid fluid, int amount) {
        super(fluid, amount);
    }

    public PressureFluidStack(Fluid fluid, int amount, CompoundTag nbt) {
        super(fluid, amount, nbt);
    }

    public PressureFluidStack(FluidStack stack, int amount) {
        super(stack, amount);
    }

    public void scalePressure(float scale) {
        setPressure(this.pressure * scale);
    }

    public void setPressure(float pressure) {
        float pressureScale = this.pressure / pressure;
        int pressurizedAmount = (int) Math.floor(getAmount() * pressureScale);
        setAmount(pressurizedAmount);
        this.pressure = pressure;
    }

    public float getPressure() {
        return pressure;
    }
}
