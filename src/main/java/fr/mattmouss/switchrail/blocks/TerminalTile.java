package fr.mattmouss.switchrail.blocks;

import com.google.common.collect.Lists;
import fr.mattmouss.switchrail.enum_rail.Corners;
import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import fr.mattmouss.switchrail.other.TerminalStorage;
import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.BlockFlags;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class TerminalTile extends TileEntity implements ITickableTileEntity, ITerminalHandler {

    private final LazyOptional<TerminalStorage> storage = LazyOptional.of(this::createStorage).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Terminal Tile Entity !");

    public TerminalTile() {
        super(ModBlock.TERMINAL_TILE);
    }

    @Nonnull
    public TerminalStorage createStorage(){
        return new TerminalStorage(this.worldPosition);
    }

    // Interface ITickableTE function
    @Override
    public void tick() {
        onTick();
    }
    //--------------

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
    public TileEntity getTile() {
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
    public void load(BlockState state, CompoundNBT nbt) {
        CompoundNBT storage_tag = nbt.getCompound("terminal");
        storage.ifPresent(terminalStorage -> ((INBTSerializable<CompoundNBT>)terminalStorage).deserializeNBT(storage_tag));
        super.load(state, nbt);
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    public CompoundNBT save(CompoundNBT nbt) {
        storage.ifPresent(terminalStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)terminalStorage).serializeNBT();
            nbt.put("terminal",compoundNBT);
        });
        return super.save(nbt);
    }

    @Override
    @Nonnull
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }
}
