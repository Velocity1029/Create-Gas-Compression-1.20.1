package com.velocity1029.create_gas_compression.blocks.compressors;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.fluids.tank.SoundPool;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlock;
import com.velocity1029.create_gas_compression.blocks.compressors.frames.CompressorFrameBlockEntity;
import com.velocity1029.create_gas_compression.blocks.compressors.guides.CompressorGuideBlock;
import com.velocity1029.create_gas_compression.registry.CGCBlocks;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;

public class CompressorData {

    static final int SAMPLE_RATE = 5;

    private static final int waterSupplyPerLevel = 10;
    private static final float passiveEngineEfficiency = 1 / 8f;

    // pooled water supply
    int gatheredSupply;
    float[] supplyOverTime = new float[10];
    int ticksUntilNextSample;
    int currentIndex;

    // heat score
    public boolean needsHeatLevelUpdate;
    public boolean passiveHeat;
    public int activeHeat;

    public Direction direction;
    public float RPM = 0;
    public int attachedGuides;
    public int activePistons;
//    public int attachedWhistles;

    // client only sound control

    // re-use the same lambda for each side
    private final SoundPool.Sound sound = (level, pos) -> {
        float volume = 3f / Math.max(2, activePistons / 6);
        float pitch = 1.18f - level.random.nextFloat() * .25f;
        level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                SoundEvents.CANDLE_EXTINGUISH, SoundSource.BLOCKS, volume, pitch, false);

        AllSoundEvents.STEAM.playAt(level, pos, volume / 16, .8f, false);
    };
    // separate pools for each side so they sound distinct when standing at corners of the boiler
    private final EnumMap<Direction, SoundPool> pools = new EnumMap<>(Direction.class);

    public void tick(CompressorFrameBlockEntity controller) {
        if (!isActive())
            return;
        Level level = controller.getLevel();
        if (level.isClientSide) {
            pools.values().forEach(p -> p.play(level));
//            gauge.tickChaser();
//            float current = gauge.getValue(1);
//            if (current > 1 && level.random.nextFloat() < 1 / 2f)
//                gauge.setValueNoUpdate(current + Math.min(-(current - 1) * level.random.nextFloat(), 0));
            return;
        }
//        if (needsHeatLevelUpdate && updateTemperature(controller))
//            controller.notifyUpdate();
        ticksUntilNextSample--;
        if (ticksUntilNextSample > 0)
            return;
//        int capacity = controller.tankInventory.getCapacity();
//        if (capacity == 0)
//            return;

//        ticksUntilNextSample = SAMPLE_RATE;
//        supplyOverTime[currentIndex] = gatheredSupply / (float) SAMPLE_RATE;
//        waterSupply = Math.max(waterSupply, supplyOverTime[currentIndex]);
//        currentIndex = (currentIndex + 1) % supplyOverTime.length;
//        gatheredSupply = 0;

//        if (currentIndex == 0) {
//            waterSupply = 0;
//            for (float i : supplyOverTime)
//                waterSupply = Math.max(i, waterSupply);
//        }

//        if (controller instanceof CreativeFluidTankBlockEntity)
//            waterSupply = waterSupplyPerLevel * 20;

//        if (getActualHeat(controller.getTotalTankSize()) == 18)
//            controller.award(AllAdvancements.STEAM_ENGINE_MAXED);

        controller.notifyUpdate();
    }

    public float getCompressorEfficiency() {
        int actualRPM = getActualRPM();
        return attachedGuides <= actualRPM ? 1 : (float) actualRPM / attachedGuides;
    }

    public int getActualRPM() {
        return (int) (6 * RPM);
    }

    public boolean isActive() {
        return attachedGuides > 0;
    }

    public void clear() {
        attachedGuides = 0;
    }

    public boolean evaluate(CompressorFrameBlockEntity controller) {
        BlockPos controllerPos = controller.getBlockPos();
        Level level = controller.getLevel();
        int prevGuides = attachedGuides;
//        int prevWhistles = attachedWhistles;
        attachedGuides = 0;
//        attachedWhistles = 0;

        for (int offset = 0; offset < controller.getWidth(); offset++) {
            BlockPos pos = controllerPos.relative(direction, offset);
            BlockState blockState = level.getBlockState(pos);
            if (!CompressorFrameBlock.isFrame(blockState))
                continue;
            for (Direction d : Iterate.directions) {
                BlockPos attachedPos = pos.relative(d);
                BlockState attachedState = level.getBlockState(attachedPos);
                if (CGCBlocks.COMPRESSOR_GUIDE.has(attachedState) && CompressorGuideBlock.getFacing(attachedState) == d)
                    attachedGuides++;
//                if (AllBlocks.STEAM_WHISTLE.has(attachedState)
//                        && WhistleBlock.getAttachedDirection(attachedState)
//                        .getOpposite() == d)
//                    attachedWhistles++;
            }
        }

//        for (int yOffset = 0; yOffset < controller.height; yOffset++) {
//            for (int xOffset = 0; xOffset < controller.width; xOffset++) {
//                for (int zOffset = 0; zOffset < controller.width; zOffset++) {
//
//                    BlockPos pos = controllerPos.offset(xOffset, yOffset, zOffset);
//                    BlockState blockState = level.getBlockState(pos);
//                    if (!FluidTankBlock.isTank(blockState))
//                        continue;
//                    for (Direction d : Iterate.directions) {
//                        BlockPos attachedPos = pos.relative(d);
//                        BlockState attachedState = level.getBlockState(attachedPos);
//                        if (AllBlocks.STEAM_ENGINE.has(attachedState) && SteamEngineBlock.getFacing(attachedState) == d)
//                            attachedEngines++;
//                        if (AllBlocks.STEAM_WHISTLE.has(attachedState)
//                                && WhistleBlock.getAttachedDirection(attachedState)
//                                .getOpposite() == d)
//                            attachedWhistles++;
//                    }
//                }
//            }
//        }

//        needsHeatLevelUpdate = true;
        return prevGuides != attachedGuides; //|| prevWhistles != attachedWhistles;
    }

    public CompoundTag write() {
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("Guides", attachedGuides);
        return nbt;
    }

    public void read(CompoundTag nbt, int compressorSize) {
        attachedGuides = nbt.getInt("Guides");
    }
}
