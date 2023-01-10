package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.CounterStorage;
import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AxleCounterTile extends TileEntity implements IPosZoomTileEntity {

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
    public LazyOptional<PosAndZoomStorage> getStorage() {
        return storage.cast();
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

    public int getAxle(){
        return storage.map(CounterStorage::getAxle).orElseThrow(storageErrorSupplier);
    }

    public boolean isFree(){
        return storage.map(CounterStorage::isFree).orElseThrow(storageErrorSupplier);
    }

    public void freePoint() {
        storage.ifPresent(CounterStorage::freePoint);
        setPowered(true);
    }

    public void addAxle() {
        if (isFree()){
            setPowered(false);
        }
        storage.ifPresent(CounterStorage::addAxle);
    }

    public void removeAxle() {
        storage.ifPresent(CounterStorage::removeAxle);
        if (isFree()){
            setPowered(true);
        }
    }

    public void setPowered(boolean powered){
        BlockState state= this.getBlockState();
        assert level != null;
        level.setBlock(worldPosition,state.setValue(BlockStateProperties.POWERED,powered), Constants.BlockFlags.DEFAULT);
    }
}


