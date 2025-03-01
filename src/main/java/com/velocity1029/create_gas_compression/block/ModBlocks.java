package com.velocity1029.create_gas_compression.block;

import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
//import net.minecraft.world.level.material.MapColor;
//import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.velocity1029.create_gas_compression.CreateGasCompression.MODID;

public class ModBlocks {
    // Create a Deferred Register to hold Items which will all be registered under the "arianes_dream" namespace
    public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);

//    public static final RegistryObject<Block> TESSERACT;
//    public static final RegistryObject<Block> DUST;
//    public static final RegistryObject<Block> DUST_BLOCK;
//    public static final RegistryObject<Block> OUTCROPPING;
//    public static final RegistryObject<Block> OBELISK;

    static {
//        TESSERACT = REGISTRY.register("tesseract", () -> new Block(BlockBehaviour.Properties.of(Material.AMETHYST).strength(10,400)));
//        DUST = REGISTRY.register("dust", () -> new SnowLayerBlock(BlockBehaviour.Properties.of(Material.TOP_SNOW).strength(0.1F,10).requiresCorrectToolForDrops().sound(SoundType.SAND).isViewBlocking((state, p_187418_, p_187419_) -> {
//            return (Integer)state.getValue(SnowLayerBlock.LAYERS) >= 8;
//        })));
//        DUST_BLOCK = REGISTRY.register("dust_block", () -> new SandBlock(11098145, BlockBehaviour.Properties.of(Material.SAND, MaterialColor.COLOR_RED).strength(0.2F,10).requiresCorrectToolForDrops().sound(SoundType.SAND)));
//        OUTCROPPING = REGISTRY.register("outcropping", () -> new Outcropping(BlockBehaviour.Properties.of(Material.METAL).strength(10,40).requiresCorrectToolForDrops().sound(SoundType.METAL).dynamicShape().noOcclusion()));
//        OBELISK = REGISTRY.register("obelisk", () -> new RotatedPillarBlock(BlockBehaviour.Properties.of(Material.AMETHYST).strength(10,40).requiresCorrectToolForDrops().sound(SoundType.METAL)));
    }

    public static void register(IEventBus eventBus) {
        REGISTRY.register(eventBus);
    }
}
