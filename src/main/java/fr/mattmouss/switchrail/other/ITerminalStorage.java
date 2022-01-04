package fr.mattmouss.switchrail.other;

import net.minecraft.util.math.BlockPos;

import java.util.Map;


public interface ITerminalStorage {
    void addSwitch(BlockPos pos);
    void removeSwitch(BlockPos pos);
    void setSwitchPosition(BlockPos pos,byte position);
    Map<BlockPos, Byte> getSwitchMap();
    void setBasePos(BlockPos pos);
    BlockPos getBasePos();
    boolean hasSwitch(BlockPos pos);
}
