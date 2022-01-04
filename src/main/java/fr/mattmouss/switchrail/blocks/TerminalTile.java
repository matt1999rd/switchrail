package fr.mattmouss.switchrail.blocks;

import com.google.common.collect.Lists;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.enum_rail.Dss_Position;
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
import net.minecraftforge.common.util.LazyOptional;

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
        if (isPowered){
            actionOnPowered();
        }else {
            freeSwitch();
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
        return storage.map(terminalStorage -> terminalStorage.getSwitchMap().get(pos)).orElseThrow(storageErrorSupplier);
    }

    public Set<BlockPos> getSwitches(){
        return storage.map(terminalStorage -> terminalStorage.getSwitchMap().keySet()).orElseThrow(storageErrorSupplier);
    }

    private void actionOnPowered(){
        blockSwitch();
        Set<BlockPos> switchPos = getSwitches();
        for (BlockPos pos : switchPos){
            assert level != null;
            BlockState state = getSwitchValue(pos);
            level.setBlock(pos,state,3);
        }
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
            assert level != null;
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof Switch && state.hasProperty(BlockStateProperties.ENABLED)){
                level.setBlock(pos,state.setValue(BlockStateProperties.ENABLED,value),2);
            }
        }
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        return super.save(nbt);
    }


    @Override
    public BlockPos getPosBase() {
        return storage.map(TerminalStorage::getBasePos).orElseThrow(storageErrorSupplier);
    }

    @Override
    public void changePosBase(Direction direction) {
        storage.ifPresent(terminalStorage -> terminalStorage.setBasePos(terminalStorage.getBasePos().relative(direction)));
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
