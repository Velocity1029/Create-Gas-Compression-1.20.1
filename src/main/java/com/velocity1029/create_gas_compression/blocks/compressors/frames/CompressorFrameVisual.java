package com.velocity1029.create_gas_compression.blocks.compressors.frames;

import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import dev.engine_room.flywheel.lib.instance.AbstractInstance;
import dev.engine_room.flywheel.lib.instance.FlatLit;
import dev.engine_room.flywheel.lib.model.Models;
import net.createmod.catnip.data.Iterate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

public class CompressorFrameVisual extends KineticBlockEntityVisual<CompressorFrameBlockEntity> {

    protected final EnumMap<Direction, RotatingInstance> keys = new EnumMap<>(Direction.class);
    protected Direction sourceFacing;

    public CompressorFrameVisual(VisualizationContext context, CompressorFrameBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        final Direction blockFacing = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);

        updateSourceFacing();

        var instancer = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT_HALF));

        for (Direction direction : Iterate.directions) {
            final Direction.Axis axis = direction.getAxis();
            if (direction != blockFacing.getOpposite()) {
                continue;
            }

            RotatingInstance instance = instancer.createInstance();

            instance.setup(blockEntity, axis, getSpeed(direction))
                    .setPosition(getVisualPosition())
                    .rotateToFace(Direction.SOUTH, direction)
                    .setChanged();

            keys.put(direction, instance);
        }
    }

    private float getSpeed(Direction direction) {
        float speed = blockEntity.getSpeed();

        if (speed != 0 && sourceFacing != null) {
            if (sourceFacing.getAxis() == direction.getAxis())
                speed *= sourceFacing == direction ? 1 : -1;
            else if (sourceFacing.getAxisDirection() == direction.getAxisDirection())
                speed *= -1;
        }
        return speed;
    }

    protected void updateSourceFacing() {
        if (blockEntity.hasSource()) {
            BlockPos source = blockEntity.source.subtract(pos);
            sourceFacing = Direction.getNearest(source.getX(), source.getY(), source.getZ());
        } else {
            sourceFacing = null;
        }
    }

    @Override
    public void update(float pt) {
        updateSourceFacing();
        for (Map.Entry<Direction, RotatingInstance> key : keys.entrySet()) {
            Direction direction = key.getKey();
            Direction.Axis axis = direction.getAxis();

            key.getValue()
                    .setup(blockEntity, axis, getSpeed(direction))
                    .setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(keys.values().toArray(FlatLit[]::new));
    }

    @Override
    protected void _delete() {
        keys.values().forEach(AbstractInstance::delete);
        keys.clear();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        keys.values()
                .forEach(consumer);
    }
}
