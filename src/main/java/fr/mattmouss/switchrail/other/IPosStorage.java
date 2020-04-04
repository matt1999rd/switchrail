package fr.mattmouss.switchrail.other;

import fr.mattmouss.switchrail.switchdata.SwitchData;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public interface IPosStorage {
    BlockPos getPos();

    void setPos(BlockPos pos);


    void setX(int i);
    void setY(int i);
    void setZ(int i);
}
