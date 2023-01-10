package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;


public class TerminalStorage extends PosAndZoomStorage {

    // the switch map store the position of all switch associated with the byte value of the switch position
    // the byte value is also storing a flag that indicates if the switch was disabled by this tile
    // byte value is as followed : 0b (0/1 -> isBlocked)*4+(0/1/2/3 -> switch position)
    // example : a switch straight registered with position turn (2nd position) and that is blocked will have byte value 5
    // a switch double slip with position all powered and that is not blocked will have byte value 3 (7 if it is blocked)
    private final Map<BlockPos,Byte> switchMap;

    public TerminalStorage(BlockPos basePos) {
        super(basePos);
        switchMap = new HashMap<>();
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = super.serializeNBT();
        ListNBT listNBT = new ListNBT();
        for (BlockPos pos : switchMap.keySet()){
            CompoundNBT switchNBT = new CompoundNBT();
            Util.putPos(switchNBT,pos);
            switchNBT.putByte("sw_position",switchMap.get(pos));
            listNBT.add(switchNBT);
        }
        tag.put("list",listNBT);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        super.deserializeNBT(nbt);
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
    }

    public void addSwitch(BlockPos pos) {
        switchMap.put(pos,(byte)0);
    }

    public void removeSwitch(BlockPos pos) {
        switchMap.remove(pos);
    }

    public void setSwitchPosition(BlockPos pos, byte position) {
        byte blockOffset = (byte) ((isSwitchBlocked(pos))?4:0);
        switchMap.replace(pos, (byte) (position+blockOffset));
    }



    public Map<BlockPos, Byte> getSwitchMap() {
        return switchMap;
    }


    public boolean hasSwitch(BlockPos pos) {
        return switchMap.containsKey(pos);
    }


    public boolean isSwitchBlocked(BlockPos pos) {
        byte value = switchMap.get(pos);
        return (value>>2 == 1);
    }


    public void setSwitchBlockingFlag(BlockPos pos, boolean blockSwitch) {
        if (isSwitchBlocked(pos) == blockSwitch){
            return;
        }
        int sign = (blockSwitch)?1:-1;
        byte value = (byte) (switchMap.get(pos)+sign*4);
        switchMap.replace(pos,value);
    }

}
