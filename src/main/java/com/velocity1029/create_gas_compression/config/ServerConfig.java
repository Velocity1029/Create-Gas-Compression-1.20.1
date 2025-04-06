package com.velocity1029.create_gas_compression.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    public final ForgeConfigSpec.ConfigValue<Integer> compressorCylinderRange;

    public ServerConfig(ForgeConfigSpec.Builder builder) {
        compressorCylinderRange = builder
                .comment("[in Blocks]",
                        "The maximum distance a compressor cylinder can push or pull liquids on either side.")
                .defineInRange("compressorCylinderRange", 256, 1, Integer.MAX_VALUE);
    }
}
