package fr.mattmouss.switchrail.axle_point;

import com.google.common.collect.Sets;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldCounterPoints extends SavedData {
    private HashMap<BlockPos, Set<CounterPoint>> counterPoints = new HashMap<>();
    private HashMap<BlockPos, UUID> cartsOnRail = new HashMap<>();

    public static final int ADD_COUNTER_PT = 0;
    public static final int REMOVE_COUNTER_PT = 1;
    public static final int TOGGLE_DIRECTION = 2;
    public static final int TOGGLE_COUNTING = 3;
    public static final int TOGGLE_BIDIRECTIONAL = 4;

    public WorldCounterPoints() {
        super();
    }

    public WorldCounterPoints(CompoundTag tag){
        counterPoints = Util.readMap(tag,"counter_pts",compoundNBT -> {
            Tag inbt = compoundNBT.get("counter_pt");
            if (!(inbt instanceof ListTag)){
                throw new IllegalStateException("Error in loading of intern set nbt : the NBT stored is not a list !");
            }
            ListTag listNBT = (ListTag) inbt;
            Set<CounterPoint> cps = new HashSet<>();
            listNBT.forEach(inbt1 -> cps.add(CounterPoint.read(inbt1)));
            return cps;
        });
        cartsOnRail = Util.readMap(tag,"cart_on_rail",compoundNBT -> compoundNBT.getUUID("cart"));
    }


    public void doActionServerSide(BlockPos cpPos,int type, CounterPoint counterPoint, BlockPos acPos, Direction side,int index) {
        Supplier<IllegalStateException> errorIfOptionalIsNull = () -> new IllegalStateException("Error in Packet transmission. Type is not inline with optional obtained !");
        switch (type){
            case ADD_COUNTER_PT:
                if (counterPoint == null) throw errorIfOptionalIsNull.get();
                this.addCounterPoint(cpPos,counterPoint);
                break;
            case REMOVE_COUNTER_PT:
                if (acPos == null || side == null) throw errorIfOptionalIsNull.get();
                this.removeCounterPoint(cpPos,acPos,side,index);
                break;
            case TOGGLE_COUNTING:
                if (acPos == null || side == null) throw errorIfOptionalIsNull.get();
                this.toggleCounting(cpPos,acPos,side,index);
                break;
            case TOGGLE_DIRECTION:
                if (acPos == null || side == null) throw errorIfOptionalIsNull.get();
                this.toggleDirection(cpPos,acPos,side,index);
                break;
            case TOGGLE_BIDIRECTIONAL:
                if (acPos == null || side == null) throw errorIfOptionalIsNull.get();
                this.toggleBidirectional(cpPos,acPos,side,index);
                break;
            default:
                throw new IllegalStateException("Expect a type within the 5 numbers possible : 0 to 4. We get the number : "+type);
        }
    }

    public void addCounterPoint(BlockPos cpPos,CounterPoint counterPoint){
        if (counterPoints.containsKey(cpPos)){
            counterPoints.get(cpPos).add(counterPoint);
        }else {
            Set<CounterPoint> cps = Sets.newHashSet(counterPoint);
            counterPoints.put(cpPos, cps);
        }
        this.setDirty();
    }

    public void removeCounterPoint(BlockPos cpPos,BlockPos acPos, Direction side,int index){
        CounterPoint point = getCounterPoint(cpPos,acPos,side,index);
        if (counterPoints.containsKey(cpPos)) {
            counterPoints.get(cpPos).remove(point);
            if (counterPoints.get(cpPos).isEmpty()){
                counterPoints.remove(cpPos);
            }
            this.setDirty();
        }else {
            Logger.getGlobal().warning("Try to remove counter point that was not present !");
        }
    }

    public void toggleDirection(BlockPos cpPos,BlockPos acPos, Direction side,int index) {
        CounterPoint point = getCounterPoint(cpPos,acPos,side,index);
        counterPoints.get(cpPos).remove(point);
        point.toggleDirection();
        counterPoints.get(cpPos).add(point);
        this.setDirty();
    }

    public void toggleCounting(BlockPos cpPos,BlockPos acPos, Direction side,int index) {
        CounterPoint point = getCounterPoint(cpPos,acPos,side,index);
        counterPoints.get(cpPos).remove(point);
        point.toggleCounting();
        counterPoints.get(cpPos).add(point);
        this.setDirty();
    }

    public void toggleBidirectional(BlockPos cpPos, BlockPos acPos, Direction side,int index){
        CounterPoint point = getCounterPoint(cpPos,acPos,side,index);
        counterPoints.get(cpPos).remove(point);
        point.toggleBidirectional();
        counterPoints.get(cpPos).add(point);
        this.setDirty();
    }

    public List<CounterPoint> getCounterPoints(BlockPos cpPos, Direction side, boolean isLeaving){
        if (counterPoints.isEmpty())return null;
        if (!counterPoints.containsKey(cpPos))return null;
        Set<CounterPoint> blockCounterPoint = counterPoints.get(cpPos);
        // we get only the counterpoint in the side given with the orientation if unidirectional and if not we just compare the counting direction
        return blockCounterPoint.stream().filter(
                cp -> cp.getCountingDirection() == side && (cp.isBidirectional() || cp.countIfMinecartArrive() == !isLeaving)
                ).collect(Collectors.toList());
    }

    public CounterPoint getCounterPoint(BlockPos cpPos,BlockPos acPos, Direction side,int index){
        Optional<CounterPoint> opt = counterPoints.get(cpPos).stream().filter(cp -> cp.test(acPos,side,index)).findAny();
        return opt.orElseThrow(() -> new IllegalStateException("Try to find a counterPoint that doesn't exists !"));
    }

    //function done when block with counterpoint are remove
    public void onCPBlockRemove(BlockPos cpPos){
        counterPoints.remove(cpPos);
    }

    //function done when an Axle counterpoint is remove
    public void onACRemove(BlockPos acPos) {
        //to avoid concurrent modification while in loop, we store all empty blockpos set in a new set
        Set<BlockPos> cpPosToRemove = new HashSet<>();
        for (BlockPos cpPos : counterPoints.keySet()){
            counterPoints.get(cpPos).removeIf(cp->cp.getACPos().equals(acPos));
            if (counterPoints.get(cpPos).isEmpty()){
                cpPosToRemove.add(cpPos);
            }
        }
        // we remove them when main loop is done
        for (BlockPos cpPos : cpPosToRemove){
            counterPoints.remove(cpPos);
        }
        this.setDirty();
    }

    // only use to make client side map for rendering
    public CounterPointInfo getCPInfo(BlockPos acPos,int index) {
        CounterPointInfo cpInfo = new CounterPointInfo();
        counterPoints.forEach((bp,cpSet) ->{
            Set<CounterPoint> cps = cpSet.stream().filter(cp->cp.testPos(acPos,index)).collect(Collectors.toSet());
            cpInfo.addCounterPoints(bp,cps);
        });
        return cpInfo;
    }

    public Optional<UUID> getCart(BlockPos cpPos){
        if (cartsOnRail.containsKey(cpPos)) return Optional.of(cartsOnRail.get(cpPos));
        else return Optional.empty();
    }

    public void onCartPassing(BlockPos cpPos,UUID uuid){
        cartsOnRail.put(cpPos,uuid);
        this.setDirty();
    }

    public void onCartLeaving(BlockPos cpPos){
        cartsOnRail.remove(cpPos);
        this.setDirty();
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        Util.writeMap(nbt,"counter_pts",counterPoints,(compoundNBT, cpSet) -> {
            ListTag listNBT = new ListTag();
            cpSet.forEach(cp -> listNBT.add(cp.write()));
            compoundNBT.put("counter_pt",listNBT);
        });
        Util.writeMap(nbt,"cart_on_rail",cartsOnRail,(compoundNBT, uuid) -> compoundNBT.putUUID("cart",uuid));
        return nbt;
    }


}
