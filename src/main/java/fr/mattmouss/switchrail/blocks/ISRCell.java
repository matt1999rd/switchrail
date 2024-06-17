package fr.mattmouss.switchrail.blocks;

import net.minecraft.util.math.BlockPos;

public interface ISRCell {
    BlockPos getPanelPos();
    int getCellIndex();
}
