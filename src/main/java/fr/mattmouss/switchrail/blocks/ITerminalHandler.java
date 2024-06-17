package fr.mattmouss.switchrail.blocks;

import com.google.common.collect.Lists;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import fr.mattmouss.switchrail.other.TerminalStorage;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.BlockState;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.Set;


public interface ITerminalHandler extends IPosZoomStorageHandler{
    LazyOptional<TerminalStorage> getTerminalStorage();
    boolean isPowered();
    boolean isBlocked();
    TileEntity getTile();
    void setBlockedFlag(boolean isBlocked);

    default boolean onTick(){
        boolean isPowered = isPowered();
        updateSwitch();
        if (isBlocked()){
            boolean isUnblocked = tryUnblockTerminal();
            if (isUnblocked && isPowered){
                actionOnPowered();
                return true;
            }
        }
        return false;
    }



    //the boolean specify if the terminal is unblocked
    default boolean tryUnblockTerminal(){
        if (!isBlocked())return true;
        boolean authoring = isSwitchActivatedByOtherTerminal();
        if (!authoring) setBlockedFlag(false);
        return !authoring;
    }

    //the boolean specify if the terminal is blocked
    default boolean tryBlockTerminal(){
        if (isBlocked())return true;
        boolean authoring = isSwitchActivatedByOtherTerminal();
        if (authoring) setBlockedFlag(true);
        return authoring;
    }

    default boolean isSwitchActivatedByOtherTerminal(){
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            BlockState state = getSwitchValue(pos);
            if (!state.getValue(BlockStateProperties.ENABLED)){
                return !getTerminalStorage().map(terminalStorage -> terminalStorage.isSwitchBlocked(pos))
                        .orElseThrow(getErrorSupplier());
            }
        }
        return false;
    }

    default void freeSwitch(BlockPos pos){
        if (hasSwitch(pos)) setEnabledProperty(pos,true);
        else System.out.println("WARNING : try to free switch that isn't bind to terminal !");
    }

    default void blockSwitch(BlockPos pos){
        if (hasSwitch(pos))setEnabledProperty(pos,false);
        else System.out.println("WARNING : try to block switch that isn't bind to terminal !");
    }

    default void freeAllSwitch() {
        Set<BlockPos> switchesPos = getSwitches();
        for (BlockPos pos : switchesPos){
            this.freeSwitch(pos);
        }
    }

    default void blockAllSwitch() {
        Set<BlockPos> switchesPos = getSwitches();
        for (BlockPos pos : switchesPos){
            this.blockSwitch(pos);
        }
    }

    default void setEnabledProperty(BlockPos pos,boolean value){
        getTerminalStorage().ifPresent(terminalStorage -> terminalStorage.setSwitchBlockingFlag(pos,!value));
        World level = getTile().getLevel();
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Switch && state.hasProperty(BlockStateProperties.ENABLED)){
            level.setBlock(pos,state.setValue(BlockStateProperties.ENABLED,value),3);
        }
    }

    default BlockState getSwitchValue(BlockPos pos){
        byte value = getSwitchByteValue(pos);
        if (value>getMaxValue(pos)){
            throw new IllegalStateException("The value to define is not in the range authorised");
        }
        World level = getTile().getLevel();
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof Switch)){
            throw new IllegalStateException("The block position given is not referring to a switch block");
        }
        Switch switchBlock = (Switch) state.getBlock();
        EnumProperty<Corners> property =switchBlock.getSwitchPositionProperty();
        Object propertyValue = property.getPossibleValues().stream().sorted().toArray()[value];
        if (property.getValueClass() == Corners.class){
            return state.setValue(property,(Corners) propertyValue);
        }else {
            return null;
        }
    }

    default byte getMaxValue(BlockPos pos){
        World level = getTile().getLevel();
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Switch){
            Switch switchBlock = (Switch) state.getBlock();
            EnumProperty<?> property =switchBlock.getSwitchPositionProperty();
            return (byte) property.getPossibleValues().size();
        }
        return 0;
    }

    default LazyOptional<PosAndZoomStorage> getStorage(){
        return getTerminalStorage().cast();
    }

    default void addSwitch(BlockPos pos){
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        // when adding a switch, we can block it only after we have added it
        storage.ifPresent(terminalStorage -> terminalStorage.addSwitch(pos));
        if (this.isPowered())blockSwitch(pos);
    }

    default void removeSwitch(BlockPos pos){
        // when removing a switch, we can free it only before we have removed it
        if (this.isPowered())freeSwitch(pos);
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        storage.ifPresent(terminalStorage -> terminalStorage.removeSwitch(pos));
    }

    default void setPosition(BlockPos pos, BlockState state){
        if (!(state.getBlock() instanceof Switch)){
            throw new IllegalStateException("The block position given is not referring to a switch block");
        }
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        Switch switchBlock = (Switch) state.getBlock();
        EnumProperty<?> property =switchBlock.getSwitchPositionProperty();
        Object value = state.getValue(property);
        int index = Lists.newArrayList(property.getPossibleValues().stream().sorted().toArray()).indexOf(value);
        storage.ifPresent(terminalStorage -> terminalStorage.setSwitchPosition(pos,(byte) index));
    }

    default boolean hasSwitch(BlockPos pos){
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        return storage.map(terminalStorage -> terminalStorage.hasSwitch(pos)).orElseThrow(getErrorSupplier());
    }

    default byte getSwitchByteValue(BlockPos pos){
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        return storage.map(terminalStorage -> (byte) (terminalStorage.getSwitchMap().get(pos)%4))
                .orElseThrow(getErrorSupplier());
    }

    default Set<BlockPos> getSwitches(){
        LazyOptional<TerminalStorage> storage = getTerminalStorage();
        return storage.map(terminalStorage -> terminalStorage.getSwitchMap().keySet()).orElseThrow(getErrorSupplier());
    }

    // action done only once when the redstone is entering the terminal block
    // can be done when a blocked terminal is released
    default void actionOnPowered(){
        World level = getTile().getLevel();
        assert level != null;
        if (tryBlockTerminal())return; // if the terminal is blocked, no switch will be blocked
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            BlockState state = getSwitchValue(pos);
            level.setBlock(pos,state, Constants.BlockFlags.DEFAULT);
        }
        this.blockAllSwitch();
    }

    // action done only once when the redstone is not entering the terminal block yet
    default void actionOnUnpowered(){ // need to check if the terminal is blocked
        boolean isBlocked = isBlocked();
        if (!isBlocked)this.freeAllSwitch(); // if the terminal was not blocked, switch has indeed been blocked and we ought to free them
        else setBlockedFlag(false); // elsewhere, we just unblock the terminal
    }

    default void updateSwitch() {
        Set<BlockPos> switches = getSwitches();
        List<BlockPos> switchToRemove = Lists.newArrayList();
        World level = getTile().getLevel();
        for (BlockPos switchPos : switches){
            assert level != null;
            if (!(level.getBlockState(switchPos).getBlock() instanceof Switch)){
                switchToRemove.add(switchPos);
            }
        }
        for (BlockPos switchPos : switchToRemove){
            removeSwitch(switchPos);
        }
    }

}
