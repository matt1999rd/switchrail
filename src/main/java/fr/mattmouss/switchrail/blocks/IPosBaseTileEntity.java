package fr.mattmouss.switchrail.blocks;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public interface IPosBaseTileEntity {
    BlockPos getPosBase();
    void changePosBase(Direction direction);
}
