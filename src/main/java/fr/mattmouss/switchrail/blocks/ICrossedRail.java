package fr.mattmouss.switchrail.blocks;

import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.core.BlockPos;

public interface ICrossedRail {
    default RailShape getRailShapeFromEntityAndState(BlockPos pos, AbstractMinecart entity) {
        return (entity.xo<pos.getX() || entity.xo> pos.getX()+1) ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH;
    }
}
