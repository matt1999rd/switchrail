package fr.mattmouss.switchrail.blocks;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IPosBaseTileEntity {
    BlockPos getBasePos();
    void changeBasePos(Direction direction);
    void setBasePos(Direction.Axis axis, int newPos);
}
