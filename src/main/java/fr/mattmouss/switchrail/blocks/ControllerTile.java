package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.PosStorage;
import fr.mattmouss.switchrail.other.PosStorageCapability;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ControllerTile extends TileEntity implements IPosBaseTileEntity {
    //we cannot store the tile entities because in order to get them we need a world object that is not yet defined when the world is being loaded

    //solution : we will store the data of each switch in a new object SwitchData which will be sent little by little via PosStorage

    private final LazyOptional<PosStorage> pos_store = LazyOptional.of(this::createPos).cast();
    private final Supplier<IllegalArgumentException> storageErrorSupplier = () -> new IllegalArgumentException("no storage found in Controller Tile Entity !");

    public ControllerTile() {
        super(ModBlock.CONTROLLER_TILE);
    }


    private PosStorage createPos(){
        return new PosStorage(this.worldPosition);
    }


    public BlockPos getPosBase() {
        return pos_store.map(PosStorage::getPos).orElseThrow(storageErrorSupplier);
    }

    public void changePosBase(Direction direction){
        pos_store.ifPresent(posStorage -> posStorage.setPos(posStorage.getPos().relative(direction)));
    }

    public void setX(int x){
        pos_store.ifPresent(posStorage -> posStorage.setX(x));
    }

    public void setY(int y){
        pos_store.ifPresent(posStorage -> posStorage.setY(y));
    }

    public void setZ(int z){
        pos_store.ifPresent(posStorage -> posStorage.setZ(z));
    }

    @Override
    public void load(BlockState state,CompoundNBT compound) {
        CompoundNBT pos_tag = compound.getCompound("pos");
        pos_store.ifPresent(switchStorage -> ((INBTSerializable<CompoundNBT>)switchStorage).deserializeNBT(pos_tag));
        super.load(state,compound);
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        pos_store.ifPresent(posStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)posStorage).serializeNBT();
            tag.put("pos",compoundNBT);
        });
        return super.save(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PosStorageCapability.POS_STORAGE_CAPABILITY){
            return pos_store.cast();
        }
        return super.getCapability(cap, side);
    }
}
