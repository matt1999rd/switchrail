package fr.mattmouss.switchrail.enum_rail;

import net.minecraft.util.IStringSerializable;

public enum Corners implements IStringSerializable {
    TURN_LEFT("turn_left",0b10),
    STRAIGHT("straight",0b00),
    TURN_RIGHT("turn_right",0b01),
    TURN("turn",0b11);


    private final String name;
    // dss active integer represent the booleans that indicates if in a given corner the left down and right up part are activated or not
    // dss active = ab where a = 1 if left down is activated and b = 1 if right up is activated
    private final int dssActive;



    Corners(String name,int dssActive) {
        this.name=name;
        this.dssActive = dssActive;
    }

    private static Corners asCorner(int dssActive){
        for (Corners corners : Corners.values()){
            if (corners.dssActive == dssActive){
                return corners;
            }
        }
        return null;
    }

    public static Corners getCorner(Dss_Position position) {
        switch (position){
            case NO_POWER:
                return STRAIGHT;
            case POWER_ON_LEFT_DOWN:
                return TURN_RIGHT;
            case POWER_ON_RIGHT_UP:
                return TURN_LEFT;
            case POWER_ON_BOTH:
                return TURN;
            default:
                return null;
        }
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public Corners moveDssSwitch(boolean isLeftDown){
        // to move from one corner to another that differ with activation of
        // left down if isLeftDown is true
        // and right up if isLeftdown is false
        // we need to toggle the corresponding bit (1 for left down and 0 for right up) in dss active integer and reform our corner object
        // to toggle a bit use the xor operator with integer 0b01 (1<<0) or 0b10 (1<<1)
        // warning : turn right is activated by the left down part and turn left is activated by the right up part (see double slip switch scheme)
        int newDssActive = this.dssActive ^ (1<<(isLeftDown?0:1));
        return Corners.asCorner(newDssActive);
    }
}
