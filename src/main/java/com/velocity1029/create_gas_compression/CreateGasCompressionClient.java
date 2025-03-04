package com.velocity1029.create_gas_compression;


import com.velocity1029.create_gas_compression.registry.CGCPartialModels;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class CreateGasCompressionClient {
    public static void onCtorClient(IEventBus modEventBus, IEventBus forgeEventBus) {
        CGCPartialModels.register();
        modEventBus.addListener(CreateGasCompressionClient::init);
    }

    public static void init(final FMLClientSetupEvent event) {
//        PonderIndex.addPlugin(new CGCPonders());
    }
}