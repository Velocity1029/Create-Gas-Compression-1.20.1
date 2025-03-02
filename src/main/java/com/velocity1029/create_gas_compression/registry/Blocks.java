package com.velocity1029.create_gas_compression.registry;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.infrastructure.config.CStress;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.velocity1029.create_gas_compression.blocks.engines.natural_gas.NaturalGasEngineBlock;
import com.velocity1029.create_gas_compression.blocks.engines.natural_gas.NaturalGasEngineGenerator;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.material.MapColor;

public class Blocks {

//    static {
//        Registrate.setCreativeTab(CreativeModeTabs.BASE_CREATIVE_TAB);
//    }

    public static final BlockEntry<NaturalGasEngineBlock> NATURAL_GAS_ENGINE =
            REGISTRATE.block("natural_gas_engine", NaturalGasEngineBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GREEN)
                            .forceSolidOn())
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(pickaxeOnly())
                    .blockstate(new NaturalGasEngineGenerator()::generate)
//                    .transform(CStress.setCapacity(16384.0))
                    .onRegister(BlockStressValues.setGeneratorSpeed(256, true))
                    .item()
                    .properties(p -> p.rarity(Rarity.EPIC))
                    .transform(customItemModel())
                    .register();

    // Load this class

    public static void register() {
    }
}
