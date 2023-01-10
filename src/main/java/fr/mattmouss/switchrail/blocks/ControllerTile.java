package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import fr.mattmouss.switchrail.other.PosAndZoomStorageCapability;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ControllerTile extends TileEntity implements IPosZoomTileEntity {
    //we cannot store the tile entities because in order to get them we need a world object that is not yet defined when the world is being loaded

    //solution : we will store the data of each switch in a new object SwitchData which will be sent little by little via PosStorage

    private final LazyOptional<PosAndZoomStorage> pos_store = LazyOptional.of(this::createPos).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Controller Tile Entity !");

    public ControllerTile() {
        super(ModBlock.CONTROLLER_TILE);
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
    public void load(@Nonnull BlockState state, CompoundNBT compound) {
        CompoundNBT pos_tag = compound.getCompound("pos");
        pos_store.ifPresent(switchStorage -> ((INBTSerializable<CompoundNBT>)switchStorage).deserializeNBT(pos_tag));
        super.load(state,compound);
    }

    @Nonnull
    @Override
    public CompoundNBT save(@Nonnull CompoundNBT tag) {
        pos_store.ifPresent(posStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)posStorage).serializeNBT();
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
    public CompoundNBT getUpdateTag() {
        return this.save(new CompoundNBT());
    }
}
