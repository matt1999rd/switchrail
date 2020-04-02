package fr.mattmouss.switchrail.blocks;

import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundEvents;

public class SwitchTile extends TileEntity implements ITickableTileEntity,IRail {

    public static Boolean stopSound = false;

    public SwitchTile() {
        super(ModBlock.SWITCH_TILE);

    }

    @Override
    public void tick() {
        verifyBlockUnderneath();

    }

    @Override
    public void verifyBlockUnderneath() {
        Block block_underneath = this.world.getBlockState(this.pos.down()).getBlock();
        if (block_underneath instanceof AirBlock){
            this.world.destroyBlock(this.pos,true);
            this.remove();
        }
    }
}
