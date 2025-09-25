package com.velocity1029.create_gas_compression.blocks.compressors.cylinders;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.velocity1029.create_gas_compression.blocks.pipes.valve.CheckValveBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.ModelFile;

public class CompressorCylinderGenerator extends SpecialBlockStateGen {

    @Override
    protected int getXRotation(BlockState state) {
        Direction flowFace = state.getValue(CompressorCylinderBlock.FACING);
        Direction guideFace = state.getValue(CompressorCylinderBlock.ATTACHED_FACE);
        int angle = 0;
        switch (flowFace) {
            case UP -> angle = 0;
            case DOWN -> angle = 180;
            case EAST, SOUTH -> angle = 270;
            case WEST, NORTH -> angle = 90;
        }

        if (guideFace == Direction.WEST || guideFace == Direction.SOUTH) {
            if (flowFace.getAxis().isHorizontal())
                angle += 180;
        }
        return angle;
    }

    @Override
    protected int getYRotation(BlockState state) {
        Direction flowFace = state.getValue(CompressorCylinderBlock.FACING);
        Direction guideFace = state.getValue(CompressorCylinderBlock.ATTACHED_FACE);

        int angle = horizontalAngle(guideFace) + 90;
        return angle;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        return AssetLookup.partialBaseModel(ctx, prov);
    }

}
