package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.other.CounterStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AxleCounterTile extends TileEntity implements ICounterHandler {

    private final LazyOptional<CounterStorage> storage = LazyOptional.of(this::createStorage);
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Axle Counter Tile Entity !");

    public AxleCounterTile() {
        super(ModBlock.AXLE_COUNTER_TILE);
    }

    @Nonnull
    public CounterStorage createStorage(){
        return new CounterStorage(this.worldPosition);
    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }

    @Override
    public void load(@Nonnull BlockState state, CompoundNBT nbt) {
        CompoundNBT storage_tag = nbt.getCompound("axle_counter");
        storage.ifPresent(ACStorage -> ((INBTSerializable<CompoundNBT>)ACStorage).deserializeNBT(storage_tag));
        super.load(state, nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT nbt) {
        storage.ifPresent(ACStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)ACStorage).serializeNBT();
            nbt.put("axle_counter",compoundNBT);
        });
        return super.save(nbt);
    }

    @Nonnull
    @Override
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }

    public void setPowered(boolean powered){
        BlockState state= this.getBlockState();
        assert level != null;
        level.setBlock(worldPosition,state.setValue(BlockStateProperties.POWERED,powered), Constants.BlockFlags.DEFAULT);
    }

    @Override
    public LazyOptional<CounterStorage> getCounterStorage() {
        return storage;
    }

    @Override
    public TileEntity getTile() {
        return this;
    }
}


