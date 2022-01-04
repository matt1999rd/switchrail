package fr.mattmouss.switchrail.enum_rail;

import net.minecraft.util.IStringSerializable;

public enum Dss_Position implements IStringSerializable {
    NO_POWER("no_power",0b00),
    POWER_ON_LEFT_DOWN("left_down_powered",0b10),
    POWER_ON_RIGHT_UP("right_up_powered",0b01),
    POWER_ON_BOTH("all_powered",0b11);


    private final String name;
    private final int dssActive;

    Dss_Position(String name,int dssActive) {
        this.name = name;
        this.dssActive = dssActive;
    }

    private static Dss_Position asDssPosition(int dssActive){
        for (Dss_Position position : Dss_Position.values()){
            if (position.dssActive == dssActive){
                return position;
            }
        }
        return null;
    }


    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Dss_Position moveDssSwitch(boolean isLeftDown){
        // to move from one corner to another that differ with activation of
        // left down if isLeftDown is true
        // and right up if isLeftdown is false
        // we need to toggle the corresponding bit (1 for left down and 0 for right up) in dss active integer and reform our corner object
        // to toggle a bit use the xor operator with integer 0b01 (1<<0) or 0b10 (1<<1)
        int newDssActive = this.dssActive ^ (1<<(isLeftDown?1:0));
        return Dss_Position.asDssPosition(newDssActive);
    }

}
