package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;



public class PosStorage implements INBTSerializable<CompoundNBT> {

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
        System.out.println("position written :"+pos);
        tag.putInt("x",pos.getX());
        tag.putInt("y",pos.getY());
        tag.putInt("z",pos.getZ());
        return tag;
    }

    public BlockPos getBasePos() {
        return pos;
    }

    public void setBasePos(BlockPos pos_in) {
        pos = pos_in;
    }
    public void setBasePos(Direction.Axis axis, int newPos) {
        this.pos = this.pos.relative(axis,newPos - this.pos.get(axis));
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("x") &&nbt.contains("y")  && nbt.contains("z") ) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            pos = new BlockPos(x,y,z);
            System.out.println("position read :"+pos);
        }
    }


}
