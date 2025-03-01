package com.velocity1029.create_gas_compression;

import com.mojang.logging.LogUtils;
import com.velocity1029.create_gas_compression.block.ModBlocks;
import com.velocity1029.create_gas_compression.item.ModItems;
//import com.velocity1029.create_gas_compression.painting.ModPaintings;
//import com.velocity1029.create_gas_compression.worldgen.biome.ModBiomes;
//import com.velocity1029.create_gas_compression.worldgen.dimension.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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

    public CreateGasCompression(FMLJavaModLoadingContext context)
    {
        IEventBus bus = context.getModEventBus();

        ModBlocks.register(bus);
        ModItems.register(bus);

//        ModPaintings.register(bus);
//
//        ModBiomes.register(bus);
//        ModDimensions.register();

        bus.addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
}
