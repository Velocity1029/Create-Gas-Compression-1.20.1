package com.velocity1029.create_gas_compression.registry;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.velocity1029.create_gas_compression.blocks.engines.EngineBlock;
import com.velocity1029.create_gas_compression.blocks.engines.EngineGenerator;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeAttachmentModel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

public class CGCBlocks {

//    static {
//        Registrate.setCreativeTab(CreativeModeTabs.BASE_CREATIVE_TAB);
//    }

    public static final BlockEntry<EngineBlock> NATURAL_GAS_ENGINE =
            REGISTRATE.block("natural_gas_engine", EngineBlock::new)
                    .initialProperties(SharedProperties::stone)
                    .properties(p -> p.mapColor(MapColor.COLOR_GREEN)
                            .forceSolidOn())
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(pickaxeOnly())
                    .blockstate(new EngineGenerator()::generate)
                    .onRegister( BlockStressValues.setGeneratorSpeed(256, true))
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<IronPipeBlock> IRON_PIPE =
            REGISTRATE.block("iron_pipe", IronPipeBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(pickaxeOnly())
                    .blockstate(BlockStateGen.pipe())
                    .onRegister( CreateRegistrate.blockModel(() -> IronPipeAttachmentModel::withAO))
                    .item()
                    .transform(customItemModel())
                    .register();

    // Load this class

    public static void register() {
    }
}
