package com.velocity1029.create_gas_compression.config;

import com.velocity1029.create_gas_compression.CreateGasCompression;
import net.minecraftforge.common.ForgeConfigSpec;

public class CreateGasCompressionConfig {
    private static final CreateGasCompressionConfig INSTANCE = new CreateGasCompressionConfig();

    private final ClientConfig client;
    private final CommonConfig common;
    private final ForgeConfigSpec clientSpec;
    private final ForgeConfigSpec commonSpec;

    public CreateGasCompressionConfig() {
        var client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        this.client = client.getLeft();
        this.clientSpec = client.getRight();
//        ForgeConfigRegistry.INSTANCE.register(CreateGasCompression.MODID, ModConfig.Type.CLIENT, client.getRight());

        var common = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        this.common = common.getLeft();
        this.commonSpec = common.getRight();
//        ForgeConfigRegistry.INSTANCE.register(CreateGasCompression.MODID, ModConfig.Type.COMMON, common.getRight());


    }

    public static ForgeConfigSpec getClientSpec() {
        return INSTANCE.clientSpec;
    }

    public static ForgeConfigSpec getCommonSpec() {
        return INSTANCE.commonSpec;
    }

    public static ClientConfig getClient() {
        return INSTANCE.client;
    }

    public static CommonConfig getCommon() {
        return INSTANCE.common;
    }

}
