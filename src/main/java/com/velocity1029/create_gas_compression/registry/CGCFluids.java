package com.velocity1029.create_gas_compression.registry;

import com.simibubi.create.content.fluids.VirtualFluid;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.velocity1029.create_gas_compression.CreateGasCompression;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

import static com.velocity1029.create_gas_compression.CreateGasCompression.REGISTRATE;

public class CGCFluids {
    static {
        REGISTRATE.setCreativeTab(CGCCreativeModeTabs.MAIN);
    }



    public static final FluidEntry<VirtualFluid>

            AIR = gas("air", "Air"),
            NATURAL_GAS = gas("natural_gas", "Natural Gas", CGCTags.CGCFluidTags.NATURAL_GAS.tag),

    CARBON_DIOXIDE = gas("carbon_dioxide", "Carbon Dioxide")
//            ETHYLENE = gas("ethylene"),
//            PROPYLENE = gas("propylene"),
//            PROPANE = gas("propane",CGCTags.CGCFluidTags.FLAMMABLE.tag),
//            BUTANE = gas("butane",CGCTags.CGCFluidTags.FLAMMABLE.tag),
//            LPG = gas("lpg",CGCTags.CGCFluidTags.LPG.tag,CGCTags.CGCFluidTags.FLAMMABLE.tag),
//            NEON = gas("neon")
                    ;

    ////

//    @SafeVarargs
//    public static FluidEntry<ForgeFlowingFluid.Flowing> fuel(String name, TagKey<Fluid>... tags) {
//
//
//        TagKey<Fluid>[] fuelTag = new TagKey[]{CGCTags.CGCFluidTags.FUEL.tag};
//
//        TagKey<Fluid>[] tagsWithFuelTag = new TagKey[tags.length+1];
//
//        System.arraycopy(tags, 0, tagsWithFuelTag, 0, tags.length);
//        System.arraycopy(fuelTag, 0, tagsWithFuelTag, tags.length, 0);
//
//        return flammableFluid(name,tags);
//
//    }

//    @SafeVarargs
//    public static FluidEntry<ForgeFlowingFluid.Flowing> flammableFluid(String name, TagKey<Fluid>... tags){
//
//
//        return  REGISTRATE.fluid(name, CreateGasCompression.asResource("fluid/"+name+"_still"),CreateGasCompression.asResource("fluid/"+name+"_flow"),
//                        FlammableFluidType.create(0x606060,
//                                () -> 1f / 24f ))
//                .lang(name)
//                .properties(b -> b.viscosity(1000)
//                        .density(1000))
//                .fluidProperties(p -> p.levelDecreasePerBlock(1)
//                        .tickRate(10)
//                        .slopeFindDistance(5)
//                        .explosionResistance(100f))
//                .tag(tags)
//                .source(FlammableFluid.Source::new)
//                .bucket()
//                .tag(CGCTags.forgeItemTag("buckets/"+name))
//                .build()
//                .register();
//    }

    @SafeVarargs
    public static FluidEntry<VirtualFluid> gas(String name, String lang_name, TagKey<Fluid>... tags){
        TagKey<Fluid> tag = FluidTags.create(CreateGasCompression.asResource(name));

        TagKey<Fluid>[] fluidTags = tags;

        if(tags.length==0){
            // Ignored, creates tags in minecraft namespace ?
            fluidTags = new TagKey[]{tag};

        }

        return  REGISTRATE.virtualFluid(name,CreateGasCompression.asResource("fluid/"+name),CreateGasCompression.asResource("fluid/"+name))
                .lang(lang_name)
                .tag(tags)
                .tag(CGCTags.CGCFluidTags.GAS.tag)
                //  .source(GasFluid.Source::new)

                .bucket()
                .lang(lang_name+" Tank")
                .tag(CGCTags.forgeItemTag("buckets/"+name))
                .build()
                .register();
    }

    public static void register() {
    }
}

