package fr.moonshade.switchrail.blocks;

import net.minecraft.entity.item.minecart.AbstractMinecartEntity;
import net.minecraft.state.properties.RailShape;
import net.minecraft.util.math.BlockPos;

public interface ICrossedRail {
    default RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecartEntity entity) {
        return (entity.xo<pos.getX() || entity.xo> pos.getX()+1) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }
}
