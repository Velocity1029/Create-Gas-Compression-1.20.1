package com.velocity1029.create_gas_compression.registry;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

import com.simibubi.create.AllDisplaySources;
import com.simibubi.create.AllMountedStorageTypes;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.fluids.tank.FluidTankMovementBehavior;
import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.BlockStateGen;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.velocity1029.create_gas_compression.blocks.engines.EngineBlock;
import com.velocity1029.create_gas_compression.blocks.engines.EngineGenerator;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.IronPipeAttachmentModel;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankBlock;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankGenerator;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankItem;
import com.velocity1029.create_gas_compression.blocks.tanks.IronTankModel;
import net.minecraft.client.renderer.RenderType;
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

    public static final BlockEntry<IronTankBlock> IRON_TANK = REGISTRATE.block("iron_tank", IronTankBlock::regular)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.noOcclusion()
            .isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate(new IronTankGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> IronTankModel::standard))
            .transform(displaySource(AllDisplaySources.BOILER))
            .transform(mountedFluidStorage(AllMountedStorageTypes.FLUID_TANK))
            .onRegister(movementBehaviour(new FluidTankMovementBehavior()))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(IronTankItem::new)
            .model(AssetLookup.customBlockItemModel("_", "block_single_window"))
            .build()
            .register();

    // Load this class

    public static void register() {
    }
}
