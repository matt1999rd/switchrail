package fr.mattmouss.switchrail.gui;

import fr.mattmouss.switchrail.blocks.ControllerTile;
import fr.mattmouss.switchrail.blocks.ModBlock;
import fr.mattmouss.switchrail.other.IPosStorage;
import fr.mattmouss.switchrail.other.PosStorageCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;

import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Objects;


public class ControllerContainer extends Container {
    private final ControllerTile tileEntity ;
    private final PlayerEntity playerEntity;

    public ControllerContainer(int windowId, World world, BlockPos pos, PlayerEntity entity) {
        super(ModBlock.CONTROLLER_CONTAINER, windowId);
        tileEntity = (ControllerTile)world.getBlockEntity(pos);
        playerEntity = entity;

        addDataSlot(new IntReferenceHolder() {
            @Override
            public int get() {
                return getX();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(PosStorageCapability.POS_STORAGE_CAPABILITY).ifPresent(s-> s.setX(i));
            }
        });
        addDataSlot(new IntReferenceHolder() {
            @Override
            public int get() {
                return getY();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(PosStorageCapability.POS_STORAGE_CAPABILITY).ifPresent(s-> s.setY(i));
            }
        });
        addDataSlot(new IntReferenceHolder() {
            @Override
            public int get() {
                return getZ();
            }

            @Override
            public void set(int i) {
                tileEntity.getCapability(PosStorageCapability.POS_STORAGE_CAPABILITY).ifPresent(s-> s.setZ(i));
            }
        });
    }

    public ControllerTile getTileEntity(){return tileEntity;}

    public int getX(){
        return tileEntity.getPosBase().getX();
    }
    public int getY(){
        return tileEntity.getPosBase().getY();
    }
    public int getZ(){
        return tileEntity.getPosBase().getZ();
    }

    public BlockPos getPos(){
        return tileEntity.getCapability(PosStorageCapability.POS_STORAGE_CAPABILITY).map(IPosStorage::getPos).orElse(new BlockPos(0,0,0));
    }



    @Override
    public boolean stillValid(PlayerEntity playerIn) {
        return stillValid(
                IWorldPosCallable.create(Objects.requireNonNull(tileEntity.getLevel()),tileEntity.getBlockPos()),
                playerEntity,
                ModBlock.CONTROLLER_BLOCK);
    }

    public void setPos(BlockPos offset) {
        tileEntity.setX(offset.getX());
        tileEntity.setY(offset.getY());
        tileEntity.setZ(offset.getZ());
    }
}
