package com.velocity1029.create_gas_compression.mixins;


import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import com.velocity1029.create_gas_compression.base.IFannable;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AirCurrent.class)
public class AirCurrentMixin {

    @Final
    @Shadow(remap = false)
    public IAirCurrentSource source;
    @Shadow(remap = false)
    public float maxDistance;
    @Shadow(remap = false)
    public Direction direction;


    @Unique
    private int create_Gas_Compression_1_20_1$getLimit() {
        if ((float) (int) maxDistance == maxDistance) {
            return (int) maxDistance;
        } else {
            return (int) maxDistance + 1;
        }
    }

    @Unique
    protected List<IFannable> create_Gas_Compression_1_20_1$fannableBlocks =
            new ArrayList<>();

    @Inject(remap = false, method = "tick", at = @At("RETURN"))
    protected void onTick(CallbackInfo ci) {
        create_Gas_Compression_1_20_1$tickAffectedFannables();
    }

    @Inject(remap = false, method = "rebuild", at = @At(value="RETURN", ordinal=0))
    protected void onRebuildClear(CallbackInfo ci) {
        create_Gas_Compression_1_20_1$fannableBlocks.clear();
    }
    @Inject(remap = false, method = "rebuild", at = @At(value="RETURN", ordinal=1))
    protected void onRebuildFindFannables(CallbackInfo ci) {
        Level world = source.getAirCurrentWorld();
        BlockPos start = source.getAirCurrentPos();
        create_Gas_Compression_1_20_1$fannableBlocks.clear();
        int limit = create_Gas_Compression_1_20_1$getLimit();
        for (int i = 1; i <= limit; i++) {
            for (int offset : Iterate.zeroAndOne) {
                BlockPos pos = start.relative(direction, i)
                        .below(offset);
                BlockEntity blockEntity =
                        world.getBlockEntity(pos);
                if (blockEntity instanceof IFannable fannableEntity) {
                    create_Gas_Compression_1_20_1$fannableBlocks.add(fannableEntity);
                }
                if (direction.getAxis()
                        .isVertical())
                    break;
            }
        }
    }

    @Unique
    public void create_Gas_Compression_1_20_1$tickAffectedFannables() {
        for (IFannable fannableBlock : create_Gas_Compression_1_20_1$fannableBlocks) {
            fannableBlock.fan(source.getAirCurrent());
        }
    }
}