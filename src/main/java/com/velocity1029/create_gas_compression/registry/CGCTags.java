package com.velocity1029.create_gas_compression.registry;

import com.velocity1029.create_gas_compression.CreateGasCompression;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collections;

public class CGCTags {

    public static <T> TagKey<T> optionalTag(IForgeRegistry<T> registry,
                                            ResourceLocation id) {
        return registry.tags()
                .createOptionalTagKey(id, Collections.emptySet());
    }

    public static <T> TagKey<T> forgeTag(IForgeRegistry<T> registry, String path) {
        return optionalTag(registry, ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    public static TagKey<Block> forgeBlockTag(String path) {
        return forgeTag(ForgeRegistries.BLOCKS, path);
    }

    public static TagKey<Item> forgeItemTag(String path) {
        return forgeTag(ForgeRegistries.ITEMS, path);
    }

    public static TagKey<Fluid> forgeFluidTag(String path) {
        return forgeTag(ForgeRegistries.FLUIDS, path);
    }

    public enum NameSpace {

        MOD(CreateGasCompression.MODID, false, true),
        FORGE("forge")


        ;

        public final String id;
        public final boolean optionalDefault;
        public final boolean alwaysDatagenDefault;

        NameSpace(String id) {
            this(id, true, false);
        }

        NameSpace(String id, boolean optionalDefault, boolean alwaysDatagenDefault) {
            this.id = id;
            this.optionalDefault = optionalDefault;
            this.alwaysDatagenDefault = alwaysDatagenDefault;
        }
    }

    // BLOCKS
    public enum CGCBlockTags {
        PRESSURIZED(NameSpace.FORGE);


        public final TagKey<Block> tag;
        public final boolean alwaysDatagen;

        CGCBlockTags() {
            this(NameSpace.MOD);
        }

        CGCBlockTags(NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        CGCBlockTags(NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        CGCBlockTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        CGCBlockTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace.id, (path == null ? Lang.asId(name()) : path));
            if (optional) {
                tag = optionalTag(ForgeRegistries.BLOCKS, id);
            } else {
                tag = BlockTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }
    }

    // FLUIDS
    public enum CGCFluidTags {
        GAS,
        FLAMMABLE,
        NATURAL_GAS(NameSpace.FORGE),

        GASOLINE(NameSpace.FORGE),
        DIESEL(NameSpace.FORGE),
        KEROSENE(NameSpace.FORGE),

        LPG(NameSpace.FORGE),
        HEAVY_OIL(NameSpace.FORGE),
        LUBRICATION_OIL(NameSpace.FORGE),
        NAPHTHA(NameSpace.FORGE),

        CRUDE_OIL(NameSpace.FORGE),

        MOLTEN_STEEL(NameSpace.FORGE),

        FUEL(NameSpace.FORGE)


        ;

        public final TagKey<Fluid> tag;
        public final boolean alwaysDatagen;

        CGCFluidTags() {
            this(NameSpace.MOD);
        }

        CGCFluidTags(NameSpace namespace) {
            this(namespace, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        CGCFluidTags(NameSpace namespace, String path) {
            this(namespace, path, namespace.optionalDefault, namespace.alwaysDatagenDefault);
        }

        CGCFluidTags(NameSpace namespace, boolean optional, boolean alwaysDatagen) {
            this(namespace, null, optional, alwaysDatagen);
        }

        CGCFluidTags(NameSpace namespace, String path, boolean optional, boolean alwaysDatagen) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(namespace.id, (path == null ? Lang.asId(name()) : path));
            if (optional) {
                tag = optionalTag(ForgeRegistries.FLUIDS, id);
            } else {
                tag = FluidTags.create(id);
            }
            this.alwaysDatagen = alwaysDatagen;
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Fluid fluid) {
            return fluid.is(tag);
        }

        public boolean matches(FluidState state) {
            return state.is(tag);
        }

        private static void init() {}

    }



    public static void init() {
        CGCFluidTags.init();
//        TFMGItemTags.init();
        CGCFluidTags.init();
//        TFMGEntityTags.init();
//        TFMGRecipeSerializerTags.init();
    }
}
