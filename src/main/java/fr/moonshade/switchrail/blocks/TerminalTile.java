package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.other.TerminalStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

public class TerminalTile extends BlockEntity implements ITerminalHandler {

    private final LazyOptional<TerminalStorage> storage = LazyOptional.of(this::createStorage).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Terminal Tile Entity !");

    public TerminalTile(BlockPos pos,BlockState state) {
        super(ModBlock.TERMINAL_TILE,pos,state);
    }

    @Nonnull
    public TerminalStorage createStorage(){
        return new TerminalStorage(this.worldPosition);
    }

    // Interface ITerminalHandler functions
    @Override
    public LazyOptional<TerminalStorage> getTerminalStorage() {
        return storage;
    }

    @Override
    public boolean isPowered() {
        return this.getBlockState().getValue(BlockStateProperties.POWERED);
    }

    @Override
    public boolean isBlocked() {
        return this.getBlockState().getValue(SwitchTerminal.IS_BLOCKED);
    }

    @Override
    public BlockEntity getTile() {
        return this;
    }

    public void setBlockedFlag(boolean blockTerminal){
        assert this.level != null;
        this.level.setBlock(this.worldPosition,this.getBlockState().setValue(SwitchTerminal.IS_BLOCKED,blockTerminal),3);
    }
    //--------------

    // Tile Entity functions

    @Override
    @ParametersAreNonnullByDefault
    public void load(CompoundTag nbt) {
        CompoundTag storage_tag = nbt.getCompound("terminal");
        storage.ifPresent(terminalStorage -> ((INBTSerializable<CompoundTag>)terminalStorage).deserializeNBT(storage_tag));
        super.load(nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        storage.ifPresent(terminalStorage -> {
            CompoundTag compoundNBT = ((INBTSerializable<CompoundTag>)terminalStorage).serializeNBT();
            nbt.put("terminal",compoundNBT);
        });
        super.saveAdditional(nbt);
    }

    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        storage.ifPresent(terminalStorage -> {
            CompoundTag compoundNBT = ((INBTSerializable<CompoundTag>)terminalStorage).serializeNBT();
            tag.put("terminal",compoundNBT);
        });
        return tag;
    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }


}
