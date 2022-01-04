package fr.mattmouss.switchrail.switchblock;

import fr.mattmouss.switchrail.blocks.ModBlock;
import net.minecraft.tileentity.TileEntity;

public class SwitchTile extends TileEntity {

    public static Boolean stopSound = false;

    public SwitchTile() {
        super(ModBlock.SWITCH_TILE);

    }

}