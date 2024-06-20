package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.CounterStorage;
import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.LazyOptional;

public interface ICounterHandler extends IPosZoomStorageHandler{
    LazyOptional<CounterStorage> getCounterStorage();
    TileEntity getTile();
    void setPowered(boolean powered);
    @Override
    default LazyOptional<PosAndZoomStorage> getStorage() {
        return getCounterStorage().cast();
    }

    default int getAxle(){
        return getCounterStorage().map(CounterStorage::getAxle).orElseThrow(getErrorSupplier());
    }

    default boolean isFree(){
        return getCounterStorage().map(CounterStorage::isFree).orElseThrow(getErrorSupplier());
    }

    default void freePoint() {
        getCounterStorage().ifPresent(CounterStorage::freePoint);
        setPowered(true);
    }

    default void addAxle() {
        if (isFree()){
            setPowered(false);
        }
        getCounterStorage().ifPresent(CounterStorage::addAxle);
    }

    default void removeAxle() {
        getCounterStorage().ifPresent(CounterStorage::removeAxle);
        if (isFree()){
            setPowered(true);
        }
    }


}
