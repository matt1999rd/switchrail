package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SwitchStraightNRight extends Switch_Straight {

    public SwitchStraightNRight(Properties builder) {
        super(builder);
        this.setRegistryName("switch_n_right");
    }

    public SwitchStraightNRight() {
        super();
        this.setRegistryName("switch_n_right");
    }

    @Override

    public RailShape getRailDirection(BlockState state, IBlockReader reader, BlockPos pos, @Nullable AbstractMinecartEntity minecartEntity) {
        Corners corner = state.get(Switch_Straight.SWITCH_POSITION);
        RailShape shape = state.get(RAIL_STRAIGHT_FLAT);
        if (corner.getName().equals("turn")){
            if (shape == RailShape.NORTH_SOUTH) {
                return RailShape.NORTH_EAST;
            }
            else if (shape == RailShape.EAST_WEST) {
                return RailShape.SOUTH_EAST;
            }
            else {
                return null;
            }

        }else {
            return shape;
        }
    }

    @Override
    public SwitchType getType() {
        return SwitchType.ST_NR;
    }
}

