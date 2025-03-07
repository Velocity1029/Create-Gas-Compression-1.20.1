package com.velocity1029.create_gas_compression.base;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import com.velocity1029.create_gas_compression.CreateGasCompression;

public class CGCSpriteShifts {
    public static final CTSpriteShiftEntry
            IRON_TANK = getCT(AllCTTypes.RECTANGLE, "fluid_tank"),
            IRON_TANK_TOP = getCT(AllCTTypes.RECTANGLE, "fluid_tank_top"),
            IRON_TANK_INNER = getCT(AllCTTypes.RECTANGLE, "fluid_tank_inner");


    //////////////////////
    public static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }
    public static CTSpriteShiftEntry horizontal(String name) {
        return getCT(AllCTTypes.HORIZONTAL_KRYPPERS, name);
    }
    private static CTSpriteShiftEntry vertical(String name) {
        return getCT(AllCTTypes.VERTICAL, name);
    }

    /////

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName, String connectedTextureName) {
        return CTSpriteShifter.getCT(type, CreateGasCompression.asResource("block/" + blockTextureName), CreateGasCompression.asResource("block/" + connectedTextureName + "_connected"));
    }

    private static CTSpriteShiftEntry getCT(CTType type, String blockTextureName) {
        return getCT(type, blockTextureName, blockTextureName);
    }
}
