package fr.mattmouss.switchrail.axle_point;

import com.google.common.collect.Sets;
import fr.mattmouss.switchrail.other.Util;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class WorldCounterPoints extends WorldSavedData {
    private HashMap<BlockPos, Set<CounterPoint>> counterPoints = new HashMap<>();
    private HashMap<BlockPos, UUID> cartsOnRail = new HashMap<>();

    public static final int ADD_COUNTER_PT = 0;
    public static final int REMOVE_COUNTER_PT = 1;
    public static final int TOGGLE_DIRECTION = 2;
    public static final int TOGGLE_COUNTING = 3;
    public static final int TOGGLE_BIDIRECTIONAL = 4;

    public WorldCounterPoints() {
        super("world_cp");
    }


    public void doActionServerSide(BlockPos cpPos,int type, Optional<CounterPoint> counterPoint, Optional<BlockPos> acPos, Optional<Direction> side) {
        Supplier<IllegalStateException> errorIfOptionalIsNull = () -> new IllegalStateException("Error in Packet transmission. Type is not inline with optional obtained !");
        switch (type){
            case ADD_COUNTER_PT:
                this.addCounterPoint(cpPos,counterPoint.orElseThrow(errorIfOptionalIsNull));
                break;
            case REMOVE_COUNTER_PT:
                this.removeCounterPoint(cpPos,acPos.orElseThrow(errorIfOptionalIsNull),side.orElseThrow(errorIfOptionalIsNull));
                break;
            case TOGGLE_COUNTING:
                this.toggleCounting(cpPos,acPos.orElseThrow(errorIfOptionalIsNull),side.orElseThrow(errorIfOptionalIsNull));
                break;
            case TOGGLE_DIRECTION:
                this.toggleDirection(cpPos,acPos.orElseThrow(errorIfOptionalIsNull),side.orElseThrow(errorIfOptionalIsNull));
                break;
            case TOGGLE_BIDIRECTIONAL:
                this.toggleBidirectional(cpPos,acPos.orElseThrow(errorIfOptionalIsNull),side.orElseThrow(errorIfOptionalIsNull));
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

    public void removeCounterPoint(BlockPos cpPos,BlockPos acPos, Direction side){
        CounterPoint point = getCounterPoint(cpPos,acPos,side);
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

    public void toggleDirection(BlockPos cpPos,BlockPos acPos, Direction side) {
        CounterPoint point = getCounterPoint(cpPos,acPos,side);
        counterPoints.get(cpPos).remove(point);
        point.toggleDirection();
        counterPoints.get(cpPos).add(point);
        this.setDirty();
    }

    public void toggleCounting(BlockPos cpPos,BlockPos acPos, Direction side) {
        CounterPoint point = getCounterPoint(cpPos,acPos,side);
        counterPoints.get(cpPos).remove(point);
        point.toggleCounting();
        counterPoints.get(cpPos).add(point);
        this.setDirty();
    }

    public void toggleBidirectional(BlockPos cpPos, BlockPos acPos, Direction side){
        CounterPoint point = getCounterPoint(cpPos,acPos,side);
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

    public CounterPoint getCounterPoint(BlockPos cpPos,BlockPos acPos, Direction side){
        Optional<CounterPoint> opt = counterPoints.get(cpPos).stream().filter(cp -> cp.test(acPos,side)).findAny();
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
    public CounterPointInfo getCPInfo(BlockPos acPos) {
        CounterPointInfo cpInfo = new CounterPointInfo();
        counterPoints.forEach((bp,cpSet) ->{
            Set<CounterPoint> cps = cpSet.stream().filter(cp->cp.getACPos().equals(acPos)).collect(Collectors.toSet());
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

    @Override
    public void load(@Nonnull CompoundNBT nbt) {
        counterPoints = Util.readMap(nbt,"counter_pts",compoundNBT -> {
            INBT inbt = compoundNBT.get("counter_pt");
            if (!(inbt instanceof ListNBT)){
                throw new IllegalStateException("Error in loading of intern set nbt : the NBT stored is not a list !");
            }
            ListNBT listNBT = (ListNBT) inbt;
            Set<CounterPoint> cps = new HashSet<>();
            listNBT.forEach(inbt1 -> cps.add(CounterPoint.read(inbt1)));
            return cps;
        });
        cartsOnRail = Util.readMap(nbt,"cart_on_rail",compoundNBT -> compoundNBT.getUUID("cart"));
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        Util.writeMap(nbt,"counter_pts",counterPoints,(compoundNBT, cpSet) -> {
            ListNBT listNBT = new ListNBT();
            cpSet.forEach(cp -> listNBT.add(cp.write()));
            compoundNBT.put("counter_pt",listNBT);
        });
        Util.writeMap(nbt,"cart_on_rail",cartsOnRail,(compoundNBT, uuid) -> compoundNBT.putUUID("cart",uuid));
        return nbt;
    }


}
