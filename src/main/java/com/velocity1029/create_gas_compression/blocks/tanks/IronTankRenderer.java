package com.velocity1029.create_gas_compression.blocks.tanks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.simibubi.create.foundation.fluid.FluidRenderer;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;

public class IronTankRenderer extends SafeBlockEntityRenderer<IronTankBlockEntity> {

    public IronTankRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    protected void renderSafe(IronTankBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
                              int light, int overlay) {
        if (!te.isController())
            return;
        if (!te.window) {
//            if (te.isDistillationTower)
//                renderAsDistillationTower(te, partialTicks, ms, buffer, light, overlay);
            return;
        }

        LerpedFloat fluidLevel = te.getFluidLevel();
        if (fluidLevel == null)
            return;

        float capHeight = 1 / 4f;
        float tankHullWidth = 1 / 16f + 1 / 128f;
        float minPuddleHeight = 1 / 16f;
        float totalHeight = te.getHeight() - 2 * capHeight - minPuddleHeight;

        float level = fluidLevel.getValue(partialTicks);
        if (level < 1 / (512f * totalHeight))
            return;
        float clampedLevel = Mth.clamp(level * totalHeight, 0, totalHeight);

        FluidTank tank = te.getTankInventory();
        FluidStack fluidStack = tank.getFluid();

        if (fluidStack.isEmpty())
            return;

        boolean top = fluidStack.getFluid()
                .getFluidType()
                .isLighterThanAir();

        float xMin = tankHullWidth;
        float xMax = xMin + te.getWidth() - 2 * tankHullWidth;
        float yMin = totalHeight + capHeight + minPuddleHeight - clampedLevel;
        float yMax = yMin + clampedLevel;

        if (top) {
            yMin += totalHeight - clampedLevel;
            yMax += totalHeight - clampedLevel;
        }

        float zMin = tankHullWidth;
        float zMax = zMin + te.getWidth() - 2 * tankHullWidth;

        ms.pushPose();
        ms.translate(0, clampedLevel - totalHeight, 0);
        FluidRenderer.renderFluidBox(fluidStack.getFluid(), fluidStack.getAmount(), xMin, yMin, zMin, xMax, yMax, zMax,
                buffer, ms, light, false, true, fluidStack.getTag());
        ms.popPose();
    }

//    protected void renderAsDistillationTower(IronTankBlockEntity te, float partialTicks, PoseStack ms, MultiBufferSource buffer,
//                                             int light, int overlay) {
//        BlockState blockState = te.getBlockState();
//        VertexConsumer vb = buffer.getBuffer(RenderType.solid());
//        ms.pushPose();
//        TransformStack msr = TransformStack.cast(ms);
//        msr.translate(te.width / 2f, 0.5, te.width / 2f);
//
//        float dialPivot = 5.75f / 16;
//
//
//        for (Direction d : Iterate.horizontalDirections) {
//            ms.pushPose();
//            CachedBufferer.partial(TFMGPartialModels.TOWER_GAUGE, blockState)
//                    .rotateY(d.toYRot())
//                    .unCentre()
//                    .translate(te.width / 2f - 6 / 16f, 0, 0)
//                    .light(light)
//                    .renderInto(ms, vb);
//            CachedBufferer.partial(AllPartialModels.BOILER_GAUGE_DIAL, blockState)
//                    .rotateY(d.toYRot())
//                    .unCentre()
//                    .translate(te.width / 2f - 6 / 16f, 0, 0)
//                    .translate(0, dialPivot, dialPivot)
//                    .rotateX(-te.visualGaugeRotation.getValue(partialTicks))
//                    .translate(0, -dialPivot, -dialPivot)
//                    .light(light)
//                    .renderInto(ms, vb);
//            ms.popPose();
//        }
//
//        ms.popPose();
//    }

    @Override
    public boolean shouldRenderOffScreen(IronTankBlockEntity te) {
        return te.isController();
    }

}