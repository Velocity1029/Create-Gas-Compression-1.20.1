package com.velocity1029.create_gas_compression;

import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
//import com.velocity1029.create_gas_compression.painting.ModPaintings;
//import com.velocity1029.create_gas_compression.worldgen.biome.ModBiomes;
//import com.velocity1029.create_gas_compression.worldgen.dimension.ModDimensions;
import com.velocity1029.create_gas_compression.compat.Mods;
import com.velocity1029.create_gas_compression.config.CreateGasCompressionConfig;
import com.velocity1029.create_gas_compression.registry.*;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CreateGasCompression.MODID)
public class CreateGasCompression
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "create_gas_compression";
    private static final Logger LOGGER = LogUtils.getLogger();

//    /**
//     * <b>Other mods should not use this field!</b> If you are an addon developer, create your own instance of
//     * {@link CreateRegistrate}.
//     */

    public static final CreateRegistrate REGISTRATE = CreateRegistrate.create(MODID)
            .defaultCreativeTab((ResourceKey<CreativeModeTab>) CGCCreativeModeTabs.MAIN.getKey());

    static {
        REGISTRATE.setTooltipModifierFactory(item ->
                new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                        .andThen(TooltipModifier.mapNull(KineticStats.create(item)))
        );
    }

    public CreateGasCompression(FMLJavaModLoadingContext context)
    {
        IEventBus modEventBus = context.getModEventBus();
        IEventBus forgeEventBus = MinecraftForge.EVENT_BUS;

        REGISTRATE.registerEventListeners(modEventBus);


        modEventBus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);


        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, CreateGasCompressionConfig.getCommonSpec());
        context.registerConfig(ModConfig.Type.CLIENT, CreateGasCompressionConfig.getClientSpec());

        REGISTRATE.setCreativeTab(CGCCreativeModeTabs.MAIN);

        CGCTags.init();
        CGCPartialModels.register();
        CGCBlocks.register();
        CGCBlockEntities.register();
        CGCItems.register();
        CGCFluids.register();
        CGCCreativeModeTabs.register(modEventBus);
//        ModPaintings.register(bus);
//
//        ModBiomes.register(bus);
//        ModDimensions.register();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> CreateGasCompressionClient.onCtorClient(modEventBus, forgeEventBus));
    }

//    public static final CreativeModeTab.Builder BUILDER = new CreativeModeTab.Builder()

//    public static final  CreativeModeTab MOD_TAB = new CreativeModeTab(MODID) {
//        @Override @NotNull
//        public ItemStack makeIcon() {
//            return ModItems.TESSERACT.get().getDefaultInstance();
//        }
//    };

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        event.enqueueWork(() -> {
            Mods.JEI.executeIfInstalled(() -> CGCRecipes::register);
//            ForgeChunkManager.setForcedChunkLoadingCallback(MODID, ChunkLoadManager::validateAllForcedChunks);
        });
        // Some common setup code
//        LOGGER.info("HELLO FROM COMMON SETUP");
//        if (Config.logDirtBlock)
//            LOGGER.info("DIRT BLOCK >> {}", ForgeRegistries.BLOCKS.getKey(Blocks.DIRT));
//        LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);
//        Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
        // Do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
            // Some client setup code
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static ResourceLocation asResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
