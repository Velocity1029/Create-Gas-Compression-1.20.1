package com.velocity1029.create_gas_compression.base;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.utility.CreateLang;
import com.velocity1029.create_gas_compression.CreateGasCompression;
import net.createmod.catnip.lang.LangBuilder;
import net.createmod.catnip.lang.LangNumberFormat;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;

public class CreateGasCompressionLang extends CreateLang {
    /**
     * legacy-ish. Use CreateGasCompressionLang.translate and other builder methods where possible
     *
     */
    public static MutableComponent translateDirect(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Component.translatable(CreateGasCompression.MODID + "." + key, args1);
    }

    public static LangBuilder builder() {
        return new LangBuilder(CreateGasCompression.MODID);
    }

    public static LangBuilder blockName(BlockState state) {
        return builder().add(state.getBlock()
                .getName());
    }

    public static LangBuilder itemName(ItemStack stack) {
        return builder().add(stack.getHoverName()
                .copy());
    }

    public static LangBuilder fluidName(FluidStack stack) {
        return builder().add(stack.getDisplayName()
                .copy());
    }

    public static LangBuilder number(double d) {
        return builder().text(LangNumberFormat.format(d));
    }

    public static LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    public static LangBuilder text(String text) {
        return builder().text(text);
    }

    @Deprecated // Use while implementing and replace all references with Lang.translate
    public static LangBuilder temporaryText(String text) {
        return builder().text(text);
    }
}
