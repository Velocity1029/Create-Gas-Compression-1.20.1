package com.velocity1029.create_gas_compression.blocks.engines.variants;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public interface IEngineVariant {
    TagKey<Fluid> getFluidTag();
    double getEnergyDensity();
    long getMaxCapacity();
    float getSpeed();
    float getStress();
}

