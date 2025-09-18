package com.velocity1029.create_gas_compression.registry;

import static net.minecraft.core.Direction.EAST;
import static net.minecraft.core.Direction.NORTH;
import static net.minecraft.core.Direction.SOUTH;
import static net.minecraft.core.Direction.UP;

import net.createmod.catnip.math.VoxelShaper;
import com.simibubi.create.AllShapes.Builder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CGCShapes {

    // Independent Shapers
    public static final VoxelShaper

    CHECK_VALVE_FLOOR = shape(4, 4, 0, 12, 12, 16).add(3, 3, 3, 13, 13, 13)
            .add(3.9, 13, 3.9, 12.1, 17, 12.1)
//			.add(5, 13, 3, 11, 14, 11)
//			.add(5, 14, 4, 11, 15, 10)
//			.add(5, 15, 5, 11, 16, 9)
//			.add(5, 16, 6, 11, 17, 8)
			.forHorizontal(SOUTH),
    CHECK_VALVE_WALL = shape(4, 0, 4, 12, 16, 12).add(3, 3, 3, 13, 13, 13)
			.add(5, 5, 13, 11, 13, 14)
			.add(5, 6, 14, 11, 12, 15)
			.add(5, 7, 15, 11, 11, 16)
			.add(5, 8, 16, 11, 10, 17)
			.forHorizontal(SOUTH),
    CHECK_VALVE_CEILING = shape(4, 4, 0, 12, 12, 16).add(3, 3, 3, 13, 13, 13)
            .add(3.9, -1, 3.9, 12.1, 3, 12.1)
//			.add(5, 2, 3, 11, 3, 11)
//			.add(5, 1, 4, 11, 2, 10)
//			.add(5, 0, 5, 11, 1, 9)
//			.add(5, -1, 6, 11, 0, 8)
			.forHorizontal(SOUTH);

    private static Builder shape(VoxelShape shape) {
        return new Builder(shape);
    }

    private static Builder shape(double x1, double y1, double z1, double x2, double y2, double z2) {
        return shape(cuboid(x1, y1, z1, x2, y2, z2));
    }

    private static VoxelShape cuboid(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }
}
