package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.AllCreativeModeTabs;
import com.velocity1029.create_gas_compression.CreateGasCompression;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraft.network.chat.Component;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class CGCCreativeModeTabs {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "create_gas_compression" namespace
    private static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CreateGasCompression.MODID);
    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.create_gas_compression.main"))
            .withTabsBefore(AllCreativeModeTabs.PALETTES_CREATIVE_TAB.getKey())
            .icon(CGCBlocks.NATURAL_GAS_ENGINE::asStack)
            .displayItems((params, output) -> {
                output.accept(CGCBlocks.NATURAL_GAS_ENGINE.asStack());
//                output.accept(CPLBlocks.ANDESITE_CHUNK_LOADER.asStack());
//                output.accept(CPLBlocks.EMPTY_BRASS_CHUNK_LOADER.asStack());
//                output.accept(CPLBlocks.BRASS_CHUNK_LOADER.asStack());
            })
            .build());

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
