package fr.mattmouss.switchrail.blocks;

import com.google.common.collect.Lists;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.Dss_Position;
import fr.mattmouss.switchrail.other.TerminalStorage;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
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
        if (isPowered){
            actionOnPowered();
        }else {
            freeSwitch();
            changeTerminalAuthoring(false);
        }
    }

    private void updateSwitch() {
        Set<BlockPos> switches = getSwitches();
        List<BlockPos> switchToRemove = Lists.newArrayList();
        for (BlockPos switchPos : switches){
            if (!(level.getBlockState(switchPos).getBlock() instanceof Switch)){
                switchToRemove.add(switchPos);
            }
        }
        for (BlockPos switchPos : switchToRemove){
            removeSwitch(switchPos);
        }
    }

    public void addSwitch(BlockPos pos){
        storage.ifPresent(terminalStorage -> terminalStorage.addSwitch(pos));
    }

    public void removeSwitch(BlockPos pos){
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
        EnumProperty<?> property =switchBlock.getSwitchPositionProperty();
        Object propertyValue = property.getPossibleValues().stream().sorted().toArray()[value];
        if (property.getValueClass() == Corners.class){
            return state.setValue((EnumProperty<Corners>)property,(Corners) propertyValue);
        }else {
            return state.setValue((EnumProperty<Dss_Position>)property,(Dss_Position) propertyValue);
        }
    }

    private byte getSwitchByteValue(BlockPos pos){
        return storage.map(terminalStorage -> (byte) (terminalStorage.getSwitchMap().get(pos)%4)).orElseThrow(storageErrorSupplier);
    }

    public Set<BlockPos> getSwitches(){
        return storage.map(terminalStorage -> terminalStorage.getSwitchMap().keySet()).orElseThrow(storageErrorSupplier);
    }

    private void actionOnPowered(){
        assert this.level != null;
        if (blockOrUnblockTerminal())return;
        blockSwitch();
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            BlockState state = getSwitchValue(pos);
            level.setBlock(pos,state,3);
        }
    }

    //the boolean specify if the terminal is blocked
    public boolean blockOrUnblockTerminal(){
        if (isSwitchActivatedByOtherTerminal()){
            changeTerminalAuthoring(true);
            return true;
        }else {
            changeTerminalAuthoring(false);
        }
        return false;
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

    private void freeSwitch(){
        setEnabledProperty(true);
    }

    private void blockSwitch(){
        setEnabledProperty(false);
    }

    private void setEnabledProperty(boolean value){
        Set<BlockPos> switches = getSwitches();
        for (BlockPos pos : switches){
            storage.ifPresent(terminalStorage -> terminalStorage.setSwitchBlockingFlag(pos,!value));
            assert level != null;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof Switch && state.hasProperty(BlockStateProperties.ENABLED)){
                level.setBlock(pos,state.setValue(BlockStateProperties.ENABLED,value),2);
            }
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
    public void changeBasePos(Direction direction) {
        storage.ifPresent(terminalStorage -> terminalStorage.setBasePos(terminalStorage.getBasePos().relative(direction)));
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
}
