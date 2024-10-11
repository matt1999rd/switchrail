package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.other.CounterStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class AxleCounterTile extends BlockEntity implements ICounterHandler {

    private final LazyOptional<CounterStorage> storage = LazyOptional.of(this::createStorage);
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Axle Counter Tile Entity !");

    public AxleCounterTile(BlockPos pos, BlockState state) {
        super(ModBlock.AXLE_COUNTER_TILE,pos,state);
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
    public void load(CompoundTag nbt) {
        CompoundTag storage_tag = nbt.getCompound("axle_counter");
        storage.ifPresent(ACStorage -> ((INBTSerializable<CompoundTag>)ACStorage).deserializeNBT(storage_tag));
        super.load(nbt);
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag nbt) {
        storage.ifPresent(ACStorage -> {
            CompoundTag compoundNBT = ((INBTSerializable<CompoundTag>)ACStorage).serializeNBT();
            nbt.put("axle_counter",compoundNBT);
        });
        return super.save(nbt);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }

    public void setPowered(boolean powered){
        BlockState state= this.getBlockState();
        assert level != null;
        level.setBlock(worldPosition,state.setValue(BlockStateProperties.POWERED,powered), 3);
    }

    @Override
    public LazyOptional<CounterStorage> getCounterStorage() {
        return storage;
    }

    @Override
    public BlockEntity getTile() {
        return this;
    }
}


