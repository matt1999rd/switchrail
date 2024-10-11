package fr.moonshade.switchrail.blocks;

import fr.moonshade.switchrail.other.PosAndZoomStorage;
import fr.moonshade.switchrail.other.PosAndZoomStorageCapability;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraft.core.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ControllerTile extends BlockEntity implements IPosZoomStorageHandler {
    //we cannot store the tile entities because in order to get them we need a world object that is not yet defined when the world is being loaded

    //solution : we will store the data of each switch in a new object SwitchData which will be sent little by little via PosStorage

    private final LazyOptional<PosAndZoomStorage> pos_store = LazyOptional.of(this::createPos).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Controller Tile Entity !");

    public ControllerTile(BlockPos pos,BlockState state) {
        super(ModBlock.CONTROLLER_TILE,pos,state);
    }


    @Nonnull
    private PosAndZoomStorage createPos(){
        return new PosAndZoomStorage(this.worldPosition);
    }


    @Override
    public LazyOptional<PosAndZoomStorage> getStorage() {
        return pos_store;
    }

    @Override
    public Supplier<IllegalArgumentException> getErrorSupplier() {
        return storageErrorSupplier;
    }

    @Override
    public void load(CompoundTag compound) {
        CompoundTag pos_tag = compound.getCompound("pos");
        pos_store.ifPresent(switchStorage -> ((INBTSerializable<CompoundTag>)switchStorage).deserializeNBT(pos_tag));
        super.load(compound);
    }

    @Nonnull
    @Override
    public CompoundTag save(@Nonnull CompoundTag tag) {
        pos_store.ifPresent(posStorage -> {
            CompoundTag compoundNBT = ((INBTSerializable<CompoundTag>)posStorage).serializeNBT();
            tag.put("pos",compoundNBT);
        });
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PosAndZoomStorageCapability.POS_AND_ZOOM_STORAGE_CAPABILITY){
            return pos_store.cast();
        }
        return super.getCapability(cap, side);
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return this.save(new CompoundTag());
    }
}
