package fr.mattmouss.switchrail.axle_point;

import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Set;

public class CounterPointInfo {
    private final HashMap<Pair<BlockPos, Direction>,Byte> info = new HashMap<>();
    // Counter point info is an object just for the GUI to save the same state as the world saved data state (we can hope !)
    public CounterPointInfo(){
    }

    public CounterPointInfo(FriendlyByteBuf buf) {
        int size = buf.readInt();
        for (int i=0;i<size;i++){
            BlockPos pos = buf.readBlockPos();
            Direction dir = buf.readEnum(Direction.class);
            byte byt = buf.readByte();
            info.put(Pair.of(pos,dir),byt);
        }
    }

    public void toBytes(FriendlyByteBuf buf){
        buf.writeInt(info.size());
        info.forEach((p,byt)->{
            buf.writeBlockPos(p.getFirst());
            buf.writeEnum(p.getSecond());
            buf.writeByte(byt);
        });
    }

    public void addCounterPoints(BlockPos cpPos, Set<CounterPoint> cps){
        for (CounterPoint cp : cps){
            addCounterPoint(cpPos,cp);
        }
    }

    public void addCounterPoint(BlockPos cpPos, CounterPoint cp){
        info.put(Pair.of(cpPos,cp.getCountingDirection()),makeFlag(cp));
    }

    public void removeCounterPoint(BlockPos cpPos, Direction side) {
        info.remove(Pair.of(cpPos,side));
    }

    public byte getCounterPointByte(BlockPos blockPos, Direction dir) {
        if (!info.containsKey(Pair.of(blockPos,dir))){
            return -1;
        }
        return info.get(Pair.of(blockPos,dir));
    }

    public boolean containsKey(BlockPos cpPos, Direction side) {
        return info.containsKey(Pair.of(cpPos,side));
    }

    public void toggleCounting(BlockPos cpPos, Direction side) {
        toggle(cpPos,side,CPFlag.ADD_AXLE);
    }

    public void toggleDirection(BlockPos cpPos, Direction side) {
        toggle(cpPos,side,CPFlag.FROM_OUTSIDE);
    }

    public void toggleBiDirectional(BlockPos cpPos,Direction side) {
        toggle(cpPos,side,CPFlag.BIDIRECTIONAL);
    }

    public void toggle(BlockPos cpPos,Direction side,CPFlag flag){
        byte byt = getCounterPointByte(cpPos,side);
        byt = (byte) (byt ^ flag.mask);
        info.replace(Pair.of(cpPos,side),byt);
    }

    public static byte makeFlag(CounterPoint cp){
        return writeFlag(cp.countIfMinecartArrive(),cp.isAddingAxle(),cp.isBidirectional());
    }

    public static byte writeFlag(boolean fromOutside,boolean addingAxle,boolean bidirectional){
        return (byte) (
                (fromOutside ? CPFlag.FROM_OUTSIDE.mask : 0) +
                (addingAxle  ? CPFlag.ADD_AXLE.mask : 0) +
                (bidirectional ? CPFlag.BIDIRECTIONAL.mask : 0)
        );
    }

    public static boolean readFlag(byte flag,CPFlag cpFlag){
        return (flag & cpFlag.mask) == cpFlag.mask;
    }



}
