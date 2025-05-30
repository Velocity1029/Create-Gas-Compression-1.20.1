package com.velocity1029.create_gas_compression.registry;

import static com.simibubi.create.api.behaviour.display.DisplaySource.displaySource;
import static com.simibubi.create.api.behaviour.movement.MovementBehaviour.movementBehaviour;
import static com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorageType.mountedFluidStorage;
import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;
import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;
import static com.velocity1029.create_gas_compression.registry.CGCTags.isPressurized;

import com.simibubi.create.*;
import com.simibubi.create.api.stress.BlockStressValues;
import com.simibubi.create.content.decoration.encasing.CasingBlock;
import com.simibubi.create.content.decoration.encasing.EncasedCTBehaviour;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import com.simibubi.create.content.fluids.pipes.EncasedPipeBlock;
import com.simibubi.create.content.fluids.tank.FluidTankMovementBehavior;
import com.simibubi.create.foundation.data.*;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.velocity1029.create_gas_compression.blocks.compressors.cylinders.CompressorCylinderBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.blocks.engines.EngineBlock;
import com.velocity1029.create_gas_compression.blocks.engines.EngineGenerator;
import com.velocity1029.create_gas_compression.blocks.heat_exchanger.CompressedGasCoolerBlock;
import com.velocity1029.create_gas_compression.blocks.pipes.EncasedIronPipeBlock;
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

    public static final BlockEntry<CasingBlock> IRON_CASING = REGISTRATE.block("iron_casing", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY))
            .transform(BuilderTransformers.casing(() -> CGCSpriteShifts.IRON_CASING))
            .transform(pickaxeOnly())
            .lang("Iron Casing")
            .register();

    public static final BlockEntry<IronPipeBlock> IRON_PIPE =
            REGISTRATE.block("iron_pipe", IronPipeBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .tag(AllTags.AllBlockTags.SAFE_NBT.tag)
                    .transform(pickaxeOnly())
                    .transform(isPressurized())
                    .blockstate(BlockStateGen.pipe())
                    .onRegister( CreateRegistrate.blockModel(() -> IronPipeAttachmentModel::withAO))
                    .item()
                    .transform(customItemModel())
                    .lang("Iron Pipe")
                    .register();

    public static final BlockEntry<EncasedIronPipeBlock> ENCASED_IRON_PIPE =
            REGISTRATE.block("encased_iron_pipe", p -> new EncasedIronPipeBlock(p, CGCBlocks.IRON_CASING::get))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.noOcclusion()
                            .mapColor(MapColor.TERRACOTTA_LIGHT_GRAY))
                    .transform(pickaxeOnly())
                    .transform(isPressurized())
                    .blockstate(BlockStateGen.encasedPipe())
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CGCSpriteShifts.IRON_CASING)))
                    .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, CGCSpriteShifts.IRON_CASING,
                            (s, f) -> !s.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
                    .onRegister(CreateRegistrate.blockModel(() -> IronPipeAttachmentModel::withAO))
                    .loot((p, b) -> p.dropOther(b, IRON_PIPE.get()))
                    .transform(EncasingRegistry.addVariantTo(IRON_PIPE))
                    .lang("Encased Iron Pipe")
                    .register();

    public static final BlockEntry<IronTankBlock> IRON_TANK = REGISTRATE.block("iron_tank", IronTankBlock::regular)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.noOcclusion()
                    .isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .transform(isPressurized())
            .blockstate(new IronTankGenerator()::generate)
            .onRegister(CreateRegistrate.blockModel(() -> IronTankModel::standard))
            .transform(displaySource(AllDisplaySources.BOILER))
            .transform(mountedFluidStorage(AllMountedStorageTypes.FLUID_TANK))
            .onRegister(movementBehaviour(new FluidTankMovementBehavior()))
            .addLayer(() -> RenderType::cutoutMipped)
            .item(IronTankItem::new)
            .model(AssetLookup.customBlockItemModel("_", "block_single_window"))
            .build()
            .lang("Iron Tank")
            .register();

    // Compressor

    public static final BlockEntry<CompressorFrameBlock> COMPRESSOR_FRAME = REGISTRATE.block("compressor_frame", CompressorFrameBlock::new)
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.noOcclusion()
                    .isRedstoneConductor((p1, p2, p3) -> true))
            .transform(pickaxeOnly())
            .blockstate((c, p) -> p.horizontalBlock(c.get(), p.models()
                    .getExistingFile(p.modLoc("block/compressor_frame/block"))))
            .addLayer(() -> RenderType::cutoutMipped)
            .item()
//            .transform(customItemModel())
            .transform(ModelGen.customItemModel("compressor_frame", "item"))
            .lang("Compressor Frame")
            .register();

    public static final BlockEntry<CompressorGuideBlock> COMPRESSOR_GUIDE =
            REGISTRATE.block("compressor_guide", CompressorGuideBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .transform(pickaxeOnly())
                    .blockstate((c, p) -> p.horizontalFaceBlock(c.get(), AssetLookup.partialBaseModel(c, p)))
                    .item()
                    .transform(customItemModel())
                    .lang("Compressor Guide")
                    .register();

    public static final BlockEntry<CompressorCylinderBlock> COMPRESSOR_CYLINDER =
            REGISTRATE.block("compressor_cylinder", CompressorCylinderBlock::new)
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.mapColor(MapColor.METAL))
                    .transform(pickaxeOnly())
                    .transform(isPressurized())
                    .blockstate(BlockStateGen.directionalBlockProviderIgnoresWaterlogged(true))
                    .simpleItem()
                    .lang("Compressor Cylinder")
                    .register();

    public static final BlockEntry<CasingBlock> HEAT_EXCHANGER = REGISTRATE.block("heat_exchanger", CasingBlock::new)
            .properties(p -> p.mapColor(MapColor.METAL))
            .transform(BuilderTransformers.casing(() -> CGCSpriteShifts.HEAT_EXCHANGER))
            .transform(pickaxeOnly())
            .lang("Heat Exchanger")
            .register();

    public static final BlockEntry<CompressedGasCoolerBlock> COMPRESSED_GAS_COOLER =

            REGISTRATE.block("compressed_gas_cooler", p -> new CompressedGasCoolerBlock(p, CGCBlocks.HEAT_EXCHANGER::get))
                    .initialProperties(() -> Blocks.IRON_BLOCK)
                    .properties(p -> p.noOcclusion()
                            .mapColor(MapColor.METAL))
                    .tag(AllTags.AllBlockTags.FAN_TRANSPARENT.tag)
                    .transform(pickaxeOnly())
                    .transform(isPressurized())
                    .blockstate(BlockStateGen.encasedPipe())
                    .onRegister(CreateRegistrate.connectedTextures(() -> new EncasedCTBehaviour(CGCSpriteShifts.HEAT_EXCHANGER)))
                    .onRegister(CreateRegistrate.casingConnectivity((block, cc) -> cc.make(block, CGCSpriteShifts.HEAT_EXCHANGER,
                            (s, f) -> !s.getValue(EncasedPipeBlock.FACING_TO_PROPERTY_MAP.get(f)))))
                    .onRegister(CreateRegistrate.blockModel(() -> IronPipeAttachmentModel::withAO))
                    .loot((p, b) -> {p.dropSelf(b); p.dropOther(b, IRON_PIPE.get());})
                    .transform(EncasingRegistry.addVariantTo(IRON_PIPE))
                    .lang("Compressed Gas Cooler")
                    .register();
//            REGISTRATE.block("heat_exchanger", HeatExchangerBlock::new)
//                    .initialProperties(() -> Blocks.IRON_BLOCK)
//                    .properties(p -> p.mapColor(MapColor.METAL))
//                    .transform(pickaxeOnly())
//                    .transform(isPressurized())
//                    .blockstate(BlockStateGen.encasedPipe())
//                    .simpleItem()
//                    .lang("Compressor Cylinder")
//                    .register();

    // Load this class

    public static void register() {
    }
}
