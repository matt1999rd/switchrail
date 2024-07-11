package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.other.PosAndZoomStorage;
import fr.mattmouss.switchrail.other.Vector2i;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.LazyOptional;

import java.util.function.Supplier;

public interface IPosZoomStorageHandler {
    // this interface is used in all rail screen block (terminal, controller, axle counter)
    LazyOptional<PosAndZoomStorage> getStorage();
    Supplier<IllegalArgumentException> getErrorSupplier();
    default BlockPos getBasePos() throws IllegalArgumentException {
        LazyOptional<PosAndZoomStorage> storage = getStorage();
        return storage.map(PosAndZoomStorage::getBasePos).orElseThrow(getErrorSupplier());
    }
    default void setBasePos(Direction.Axis axis, int newPos){
        LazyOptional<PosAndZoomStorage> storage = getStorage();
        storage.ifPresent(st->st.setBasePos(axis,newPos));
    }
    default Vector2i getZoom(){
        LazyOptional<PosAndZoomStorage> storage = getStorage();
        return storage.map(PosAndZoomStorage::getZoom).orElseThrow(getErrorSupplier());
    }

    default void setZoomX(int x){
        LazyOptional<PosAndZoomStorage> storage = getStorage();
        storage.ifPresent(st -> st.setZoomX(x));
    }

    default void setZoomY(int y){
        LazyOptional<PosAndZoomStorage> storage = getStorage();
        storage.ifPresent(st -> st.setZoomY(y));
    }
}
