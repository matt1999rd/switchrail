package fr.mattmouss.switchrail.switchblock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class SwitchStraightVRight extends SwitchStraight {

    public SwitchStraightVRight() {
        super(true);
        this.setRegistryName("switch_v_right");
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
