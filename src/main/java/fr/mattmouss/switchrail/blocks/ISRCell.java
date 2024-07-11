package fr.mattmouss.switchrail.blocks;

import net.minecraft.core.BlockPos;

public interface ISRCell {
    BlockPos getPanelPos();
    int getCellIndex();
}
