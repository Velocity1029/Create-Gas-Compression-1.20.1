package com.velocity1029.create_gas_compression.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public final ForgeConfigSpec.ConfigValue<Integer> compressorCylinderRange;
    public final ForgeConfigSpec.ConfigValue<Integer> pressurizedFluidRange;
    public final ForgeConfigSpec.ConfigValue<Integer> maximumPressureStages;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        compressorCylinderRange = builder
                .comment("[in Blocks]",
                        "The maximum distance a compressor cylinder can push or pull liquids on either side.")
                .defineInRange("compressorCylinderRange", 256, 1, Integer.MAX_VALUE);
        pressurizedFluidRange = builder
                .comment("[in Blocks]",
                        "The maximum distance a compressed fluid will flow out of a pressurized container.",
                        "Only a fluid at the maximum pressure will reach max range.")
                .defineInRange("pressurizedFluidRange", 256, 1, Integer.MAX_VALUE);
        maximumPressureStages = builder
                .comment("The maximum number of pressurization stages achievable using multiple compressors.",
                        "Each stage doubles the pressure of the fluid (1 stage = 2 times pressure, 3 stages = 8 times pressure).")
                .defineInRange("maximumFluidPressure", 3, 0, Integer.MAX_VALUE);
    }
}
