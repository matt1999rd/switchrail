package fr.mattmouss.switchrail.blocks;

import com.google.common.collect.Lists;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.other.TerminalStorage;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class TerminalTile extends TileEntity implements ITickableTileEntity,IPosBaseTileEntity {

    private final LazyOptional<TerminalStorage> storage = LazyOptional.of(this::createStorage).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Terminal Tile Entity !");

    public TerminalTile() {
        super(ModBlock.TERMINAL_TILE);
    }

    public TerminalStorage createStorage(){
        return new TerminalStorage(this.worldPosition);
    }

    @Override
    public void tick() {
        BlockState state = this.getBlockState();
        boolean isPowered = state.getValue(BlockStateProperties.POWERED);
        updateSwitch();
        if (this.getBlockState().getValue(SwitchTerminal.IS_BLOCKED)){
            boolean isUnblocked = tryUnblockTerminal();
            if (isUnblocked && isPowered){
                actionOnPowered();
            }
        }
    }

    private void updateSwitch() {
        Set<BlockPos> switches = getSwitches();
        List<BlockPos> switchToRemove = Lists.newArrayList();
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

    public void addSwitch(BlockPos pos){
        // when adding a switch, we can block it only after we have added it
        storage.ifPresent(terminalStorage -> terminalStorage.addSwitch(pos));
        if (this.getBlockState().getValue(BlockStateProperties.POWERED))blockSwitch(pos);
    }

    public void removeSwitch(BlockPos pos){
        // when removing a switch, we can free it only before we have removed it
        if (this.getBlockState().getValue(BlockStateProperties.POWERED))freeSwitch(pos);
        storage.ifPresent(terminalStorage -> terminalStorage.removeSwitch(pos));
    }

    public void setPosition(BlockPos pos,BlockState state){
        if (!(state.getBlock() instanceof Switch)){
            throw new IllegalStateException("The block position given is not referring to a switch block");
        }
        Switch switchBlock = (Switch) state.getBlock();
        EnumProperty<?> property =switchBlock.getSwitchPositionProperty();
        Object value = state.getValue(property);
        int index = Lists.newArrayList(property.getPossibleValues().stream().sorted().toArray()).indexOf(value);
        storage.ifPresent(terminalStorage -> terminalStorage.setSwitchPosition(pos,(byte) index));
    }

    public boolean hasSwitch(BlockPos pos){
        return storage.map(terminalStorage -> terminalStorage.hasSwitch(pos)).orElseThrow(storageErrorSupplier);
    }

    public BlockState getSwitchValue(BlockPos pos){
        byte value = getSwitchByteValue(pos);
        if (value>getMaxValue(pos)){
            throw new IllegalStateException("The value to define is not in the range authorised");
        }
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

    private byte getSwitchByteValue(BlockPos pos){
        return storage.map(terminalStorage -> (byte) (terminalStorage.getSwitchMap().get(pos)%4)).orElseThrow(storageErrorSupplier);
    }

    public Set<BlockPos> getSwitches(){
        return storage.map(terminalStorage -> terminalStorage.getSwitchMap().keySet()).orElseThrow(storageErrorSupplier);
    }

    // action done only once when the redstone is entering the terminal block
    // can be done when a blocked terminal is released
    public void actionOnPowered(){
        assert this.level != null;
        if (tryBlockTerminal())return; // if the terminal is blocked, no switch will be blocked
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            BlockState state = getSwitchValue(pos);
            level.setBlock(pos,state, BlockFlags.DEFAULT);
        }
        this.blockAllSwitch();
    }

    // action done only once when the redstone is not entering the terminal block yet
    public void actionOnUnpowered(){ // need to check if the terminal is blocked
        boolean isBlocked = this.getBlockState().getValue(SwitchTerminal.IS_BLOCKED);
        if (!isBlocked)this.freeAllSwitch(); // if the terminal was not blocked, switch has indeed been blocked and we ought to free them
        else changeTerminalAuthoring(false); // elsewhere, we just unblock the terminal
    }

    //the boolean specify if the terminal is unblocked
    public boolean tryUnblockTerminal(){
        if (!this.getBlockState().getValue(SwitchTerminal.IS_BLOCKED))return true;
        boolean authoring = isSwitchActivatedByOtherTerminal();
        if (!authoring)changeTerminalAuthoring(false);
        return !authoring;
    }

    //the boolean specify if the terminal is blocked
    public boolean tryBlockTerminal(){
        if (this.getBlockState().getValue(SwitchTerminal.IS_BLOCKED))return true;
        boolean authoring = isSwitchActivatedByOtherTerminal();
        if (authoring)changeTerminalAuthoring(true);
        return authoring;
    }

    private void changeTerminalAuthoring(boolean blockTerminal){
        assert this.level != null;
        this.level.setBlock(this.worldPosition,this.getBlockState().setValue(SwitchTerminal.IS_BLOCKED,blockTerminal),3);
    }

    private boolean isSwitchActivatedByOtherTerminal(){
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            BlockState state = getSwitchValue(pos);
            if (!state.getValue(BlockStateProperties.ENABLED)){
                return !storage.map(terminalStorage -> terminalStorage.isSwitchBlocked(pos)).orElseThrow(storageErrorSupplier);
            }
        }
        return false;
    }

    public void freeSwitch(BlockPos pos){
        if (hasSwitch(pos)) setEnabledProperty(pos,true);
        else System.out.println("WARNING : try to free switch that isn't bind to terminal !");
    }

    public void blockSwitch(BlockPos pos){
        if (hasSwitch(pos))setEnabledProperty(pos,false);
        else System.out.println("WARNING : try to block switch that isn't bind to terminal !");
    }

    private void setEnabledProperty(BlockPos pos,boolean value){
        storage.ifPresent(terminalStorage -> terminalStorage.setSwitchBlockingFlag(pos,!value));
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Switch && state.hasProperty(BlockStateProperties.ENABLED)){
                level.setBlock(pos,state.setValue(BlockStateProperties.ENABLED,value),3);
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        CompoundNBT storage_tag = nbt.getCompound("terminal");
        storage.ifPresent(switchStorage -> ((INBTSerializable<CompoundNBT>)switchStorage).deserializeNBT(storage_tag));
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        storage.ifPresent(posStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)posStorage).serializeNBT();
            nbt.put("terminal",compoundNBT);
        });
        return super.save(nbt);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }


    @Override
    public BlockPos getBasePos() {
        return storage.map(TerminalStorage::getBasePos).orElseThrow(storageErrorSupplier);
    }

    @Override
    public void setBasePos(Direction.Axis axis, int newPos) {
        storage.ifPresent(storage->storage.setBasePos(axis,newPos));
    }

    public byte getMaxValue(BlockPos pos){
        assert level != null;
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof Switch){
            Switch switchBlock = (Switch) state.getBlock();
            EnumProperty<?> property =switchBlock.getSwitchPositionProperty();
            return (byte) property.getPossibleValues().size();
        }
        return 0;
    }

    public void freeAllSwitch() {
        Set<BlockPos> switchesPos = getSwitches();
        for (BlockPos pos : switchesPos){
            this.freeSwitch(pos);
        }
    }

    public void blockAllSwitch() {
        Set<BlockPos> switchesPos = getSwitches();
        for (BlockPos pos : switchesPos){
            this.blockSwitch(pos);
        }
    }
}
