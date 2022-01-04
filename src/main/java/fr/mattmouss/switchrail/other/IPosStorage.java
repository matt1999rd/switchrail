package fr.mattmouss.switchrail.other;

import net.minecraft.util.math.BlockPos;


public interface IPosStorage {
    BlockPos getPos();

    void setPos(BlockPos pos);


    void setX(int i);
    void setY(int i);
    void setZ(int i);
}
