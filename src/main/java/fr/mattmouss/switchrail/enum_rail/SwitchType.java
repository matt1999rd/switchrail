package fr.mattmouss.switchrail.enum_rail;

import net.minecraft.util.IStringSerializable;

public enum SwitchType implements IStringSerializable {
    TJS(0,"TJS"),
    TJD(1,"TJD"),
    DOUBLE_TURN(2,"DOUBLE_TURN"),
    ST_NL(3,"ST_NL"),
    ST_NR(4,"ST_NR"),
    ST_VL(5,"ST_VL"),
    ST_VR(6,"ST_VR"),
    TRIPLE(7,"TRIPLE");


    private final int meta;

    private final String name;


    SwitchType(int meta,String name) {
        this.meta=meta;
        this.name=name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public int getMeta(){
        return this.meta;
    }

    public boolean isDoubleSwitch() {
        return (this.meta != 7)  && (this.meta != 1);
    }
}
