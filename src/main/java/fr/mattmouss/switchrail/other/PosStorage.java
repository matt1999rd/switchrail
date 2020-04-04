package fr.mattmouss.switchrail.other;

import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchdata.SwitchData;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;


import java.util.ArrayList;
import java.util.List;

public class PosStorage implements INBTSerializable<CompoundNBT>, IPosStorage {

    private BlockPos pos;

    public PosStorage(BlockPos te_pos) {
        pos = te_pos;
    }

    public PosStorage() {
        pos = new BlockPos(0,0,0);
    }



    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        tag.putInt("x",pos.getX());
        tag.putInt("y",pos.getY());
        tag.putInt("z",pos.getZ());
        return tag;
    }

    public BlockPos getPos() {
        return pos;
    }

    @Override
    public void setPos(BlockPos pos_in) {
        pos = pos_in;
    }

    public void setX(int x){
        pos = new BlockPos(x,pos.getY(),pos.getZ());
    }

    public void setY(int y){
        pos = new BlockPos(pos.getX(),y,pos.getZ());
    }
    public void setZ(int z){
        pos = new BlockPos(pos.getX(),pos.getY(),z);
    }


    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("x") &&nbt.contains("y")  && nbt.contains("z") ) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            pos = new BlockPos(x,y,z);
        }
    }


}
