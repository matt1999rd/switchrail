package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;


public class TerminalStorage implements INBTSerializable<CompoundNBT>, ITerminalStorage {

    private final Map<BlockPos,Byte> switchMap;

    private BlockPos basePos;

    public TerminalStorage(BlockPos basePos) {
        this();
        this.basePos = basePos;
    }

    public TerminalStorage(){
        switchMap = new HashMap<>();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT listNBT = new ListNBT();
        for (BlockPos pos : switchMap.keySet()){
            CompoundNBT switchNBT = new CompoundNBT();
            Util.putPos(switchNBT,pos);
            switchNBT.putByte("sw_position",switchMap.get(pos));
            listNBT.add(switchNBT);
        }
        tag.put("list",listNBT);
        tag.putLong("base_pos",basePos.asLong());
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if (nbt.contains("list")){
            ListNBT listNBT = (ListNBT) nbt.get("list");
            assert listNBT != null;
            for (INBT inbt : listNBT){
                CompoundNBT sw_nbt = (CompoundNBT) inbt;
                BlockPos pos = Util.getPosFromNbt(sw_nbt);
                byte switchPosition = sw_nbt.getByte("sw_position");
                switchMap.put(pos,switchPosition);
            }
        }
        basePos = BlockPos.of(nbt.getLong("base_pos"));
    }


    @Override
    public void addSwitch(BlockPos pos) {
        switchMap.put(pos,(byte)0);
    }

    @Override
    public void removeSwitch(BlockPos pos) {
        switchMap.remove(pos);
    }

    @Override
    public void setSwitchPosition(BlockPos pos, byte position) {
        switchMap.replace(pos,position);
    }



    @Override
    public Map<BlockPos, Byte> getSwitchMap() {
        return switchMap;
    }

    @Override
    public void setBasePos(BlockPos pos) {
        this.basePos = pos;
    }

    @Override
    public BlockPos getBasePos() {
        return basePos;
    }

    @Override
    public boolean hasSwitch(BlockPos pos) {
        return switchMap.containsKey(pos);
    }
}
