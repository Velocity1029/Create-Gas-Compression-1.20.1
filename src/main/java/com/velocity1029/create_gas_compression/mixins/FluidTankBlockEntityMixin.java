package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.velocity1029.create_gas_compression.base.FluidTankBlockEntityAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FluidTankBlockEntity.class)
public abstract class FluidTankBlockEntityMixin implements FluidTankBlockEntityAccessor {
    @Shadow
    protected boolean window;

    public boolean getWindow() {
        return window;
    }

    @Shadow(remap = false)
    protected int luminosity;

    public int getLuminosity() {
        return luminosity;
    }
}
