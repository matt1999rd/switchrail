package fr.mattmouss.switchrail.enum_rail;

import net.minecraft.util.IStringSerializable;

public enum Tjd_Position implements IStringSerializable {
    NO_POWER(0,"no_power"),
    POWER_ON_RIGHT_UP(1,"right_up_powered"),
    POWER_ON_LEFT_DOWN(2,"left_down_powered"),
    POWER_ON_BOTH(3,"all_powered");


    private final int meta;
    private final String name;

    private Tjd_Position(int meta, String name) {
        this.meta = meta;
        this.name = name;
    }


    @Override
    public String getName() {
        return this.name;
    }
}
