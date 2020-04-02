package fr.mattmouss.switchrail.railflatshape;

import net.minecraft.state.properties.RailShape;
import net.minecraft.util.IStringSerializable;

public enum RailFlatShape implements IStringSerializable {
    NORTH_SOUTH(0,"north_south"),
    EAST_WEST(1,"east_west");

    private final int meta;

    private final String name;


    private RailFlatShape(int meta,String name) {
        this.meta=meta;
        this.name=name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public RailShape toRailShape(){
        if (this.meta ==0){
            return RailShape.NORTH_SOUTH;
        }else {
            return RailShape.EAST_WEST;
        }
    }
}
