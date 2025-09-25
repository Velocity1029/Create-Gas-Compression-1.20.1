package com.velocity1029.create_gas_compression.blocks.pipes.valve;

import com.simibubi.create.foundation.data.AssetLookup;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraftforge.client.model.generators.ModelFile;

public class CheckValveGenerator extends SpecialBlockStateGen {

    @Override
    protected int getXRotation(BlockState state) {
        Direction valveFace = state.getValue(CheckValveBlock.VALVE);
        Direction flowFace = state.getValue(CheckValveBlock.FLOW);
        return valveFace == Direction.DOWN ? 180
                : valveFace == Direction.UP ? 0
                : flowFace == Direction.DOWN ? 90
                : flowFace == Direction.UP ? 270
                : 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        Direction valveFace = state.getValue(CheckValveBlock.VALVE);
        Direction flowFace = state.getValue(CheckValveBlock.FLOW);
        int angle = flowFace.getAxis().isVertical()
                ? valveFace.getAxisDirection().getStep() == flowFace.getAxisDirection().getStep() ? 0 : 180
                : valveFace.getAxisDirection().getStep() == flowFace.getAxisDirection().getStep() ? 180 : 0;
        if (valveFace.getAxis() == Direction.Axis.Y && flowFace.getAxis() == Direction.Axis.X
            || valveFace.getAxis() == Direction.Axis.X && flowFace.getAxis() == Direction.Axis.Y)
            angle -= 90;
        return angle;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        return AssetLookup.partialBaseModel(ctx, prov);
    }

}
