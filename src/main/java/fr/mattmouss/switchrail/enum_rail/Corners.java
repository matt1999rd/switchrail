package fr.mattmouss.switchrail.enum_rail;

import net.minecraft.util.IStringSerializable;

public enum Corners implements IStringSerializable {
    STRAIGHT(0,"straight"),
    TURN(1,"turn"),
    TURN_LEFT(2,"turn_left"),
    TURN_RIGHT(3,"turn_right");


    private final int meta ;
    private final String name;



    private Corners(int meta, String name) {
        this.meta=meta;
        this.name=name;

    }

    @Override
    public String getName() {
        return this.name;
    }
}
