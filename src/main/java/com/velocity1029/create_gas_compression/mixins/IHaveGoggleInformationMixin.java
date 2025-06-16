package com.velocity1029.create_gas_compression.mixins;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.CreateLang;
import com.velocity1029.create_gas_compression.base.CreateGasCompressionLang;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.Optional;

@Mixin(IHaveGoggleInformation.class)
public interface IHaveGoggleInformationMixin {
    /**
     * @author Velocity1029
     * @reason Pressure tooltips
     */
    @Overwrite( remap = false)
    default boolean containedFluidTooltip(List<Component> tooltip, boolean isPlayerSneaking,
                                          LazyOptional<IFluidHandler> handler) {
        Optional<IFluidHandler> resolve = handler.resolve();
        if (!resolve.isPresent())
            return false;

        IFluidHandler tank = resolve.get();
        if (tank.getTanks() == 0)
            return false;

        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);

        boolean isEmpty = true;
        for (int i = 0; i < tank.getTanks(); i++) {
            FluidStack fluidStack = tank.getFluidInTank(i);
            if (fluidStack.isEmpty())
                continue;

            CreateLang.fluidName(fluidStack)
                    .style(ChatFormatting.GRAY)
                    .forGoggles(tooltip, 1);

            CreateLang.builder()
                    .add(CreateLang.number(fluidStack.getAmount())
                            .add(mb)
                            .style(ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(tank.getTankCapacity(i))
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);

            if (fluidStack.hasTag() && fluidStack.getTag().getFloat("Pressure") != 0.0F) {
                LangBuilder psi = CreateGasCompressionLang.translate("generic.unit.pressure").style(ChatFormatting.GRAY);
                float pressure = fluidStack.getTag().getFloat("Pressure");
                boolean hot = fluidStack.getTag().getBoolean("Hot");

                CreateGasCompressionLang.builder()
                        .add(CreateGasCompressionLang.number(pressure)
                                .text("x ")
                                .style(ChatFormatting.AQUA))
                        .add(psi)
                        .text(" ")
                        .add(
                            hot ? CreateGasCompressionLang.translate("gui.goggles.fluid_hot")
                                .style(ChatFormatting.RED)
                                : CreateGasCompressionLang.translate("gui.goggles.fluid_cool")
                                .style(ChatFormatting.DARK_BLUE))
                        .forGoggles(tooltip, 1);
            }

            isEmpty = false;
        }

        if (tank.getTanks() > 1) {
            if (isEmpty)
                tooltip.remove(tooltip.size() - 1);
            return true;
        }

        if (!isEmpty)
            return true;

        CreateLang.translate("gui.goggles.fluid_container.capacity")
                .add(CreateLang.number(tank.getTankCapacity(0))
                        .add(mb)
                        .style(ChatFormatting.GOLD))
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);

        return true;
    }
}
