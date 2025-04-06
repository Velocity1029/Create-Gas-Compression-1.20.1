package com.velocity1029.create_gas_compression.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class CreateGasCompressionConfig {
    private static final CreateGasCompressionConfig INSTANCE = new CreateGasCompressionConfig();

    private final ClientConfig client;
    private final CommonConfig common;
    private final ServerConfig server;
    private final ForgeConfigSpec clientSpec;
    private final ForgeConfigSpec commonSpec;
    private final ForgeConfigSpec serverSpec;

    public CreateGasCompressionConfig() {
        var client = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
        this.client = client.getLeft();
        this.clientSpec = client.getRight();
//        ForgeConfigRegistry.INSTANCE.register(CreateGasCompression.MODID, ModConfig.Type.CLIENT, client.getRight());

        var common = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        this.common = common.getLeft();
        this.commonSpec = common.getRight();
//        ForgeConfigRegistry.INSTANCE.register(CreateGasCompression.MODID, ModConfig.Type.COMMON, common.getRight());

        var server = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        this.server = server.getLeft();
        this.serverSpec = server.getRight();


    }

    public static ForgeConfigSpec getClientSpec() {
        return INSTANCE.clientSpec;
    }

    public static ForgeConfigSpec getCommonSpec() {
        return INSTANCE.commonSpec;
    }

    public static ForgeConfigSpec getServerSpec() {
        return INSTANCE.serverSpec;
    }

    public static ClientConfig getClient() {
        return INSTANCE.client;
    }

    public static CommonConfig getCommon() {
        return INSTANCE.common;
    }

    public static ServerConfig getServer() {
        return INSTANCE.server;
    }

}
