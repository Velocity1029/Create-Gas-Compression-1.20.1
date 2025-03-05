package com.velocity1029.create_gas_compression.blocks.engines.variants;

import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import com.velocity1029.create_gas_compression.registry.CGCFluids;
import com.velocity1029.create_gas_compression.registry.CGCTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class NaturalGasVariant implements IEngineVariant {
    @Override
    public TagKey<Fluid> getFuelTag() {
        return CGCTags.CGCFluidTags.NATURAL_GAS.tag;
    }
    @Override
    public Fluid getExhaust() {
        return CGCFluids.CARBON_DIOXIDE.getSource();
    }

    @Override
    public double getEnergyDensity() {
        return CreateGasCompressionConfig.getCommon().naturalGasEnergyDensity.get();
    }

    @Override
    public long getMaxCapacity() {
        return CreateGasCompressionConfig.getCommon().naturalGasEngineCapacity.get();
    }

    @Override
    public float getSpeed() {
        return CreateGasCompressionConfig.getCommon().naturalGasEngineSpeed.get().floatValue();
    }

    @Override
    public float getStress() {
        return CreateGasCompressionConfig.getCommon().naturalGasEngineStress.get().floatValue();
    }
}
