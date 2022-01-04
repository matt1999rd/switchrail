package fr.mattmouss.switchrail.switchblock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SwitchStraightNLeft extends SwitchStraight {

    public SwitchStraightNLeft() {
        super(true);
        this.setRegistryName("switch_n_left");
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SwitchTile();
    }
}
