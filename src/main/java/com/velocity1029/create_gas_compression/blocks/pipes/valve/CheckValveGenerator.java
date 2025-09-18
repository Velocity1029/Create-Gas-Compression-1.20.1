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
        return valveFace == Direction.DOWN ? 180 : valveFace == Direction.UP ? 0 : 270;
//        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        Direction valveFace = state.getValue(CheckValveBlock.VALVE);
        int angle = horizontalAngle(state.getValue(CheckValveBlock.FLOW));
        angle += horizontalAngle(valveFace);
        return angle + (valveFace == Direction.DOWN ? 180 : 0);
//        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov,
                                                BlockState state) {
        return AssetLookup.partialBaseModel(ctx, prov);
    }

}
