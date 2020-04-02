package fr.mattmouss.switchrail.gui;

import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.ModBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;


public class ControllerContainer extends Container {
    private ControllerTile tileEntity ;
    private PlayerEntity playerEntity;

    public ControllerContainer(int windowId, World world, BlockPos pos, PlayerEntity entity) {
        super(ModBlock.CONTROLLER_CONTAINER, windowId);
        tileEntity = (ControllerTile)world.getTileEntity(pos);
        playerEntity = entity;
    }

    public ControllerTile getTileEntity(){return tileEntity;}

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return isWithinUsableDistance(
                IWorldPosCallable.of(tileEntity.getWorld(),tileEntity.getPos()),
                playerEntity,
                ModBlock.CONTROLLER_BLOCK);
    }
}
