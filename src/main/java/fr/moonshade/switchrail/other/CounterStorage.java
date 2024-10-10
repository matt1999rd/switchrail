package fr.moonshade.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;

import java.util.logging.Logger;

public class CounterStorage extends PosAndZoomStorage {
    int axle_number;

    public CounterStorage(BlockPos basePos){
         super(basePos);
         this.axle_number = 0;
    }

    public CounterStorage(){
        super();
        this.axle_number = 0;
    }

    public int getAxle(){
         return axle_number;
     }

    public boolean isFree(){
         return axle_number == 0;
    }

    public void freePoint(){
         axle_number = 0;
     }

    public void addAxle() { axle_number++; }

    public void removeAxle() {
        if (axle_number <= 0){
            axle_number = 0;
            Logger.getGlobal().warning("Axle number is already 0 : try to remove non existing axle !");
        }else {
            axle_number--;
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT nbt = super.serializeNBT();
        nbt.putInt("axle",axle_number);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
        axle_number = nbt.getInt("axle");
    }


}
