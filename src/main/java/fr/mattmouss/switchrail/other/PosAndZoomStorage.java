package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;

import static fr.mattmouss.switchrail.other.Util.DEFAULT_ZOOM;


public class PosAndZoomStorage implements INBTSerializable<CompoundTag> {
    private BlockPos pos;
    private Vector2i zoom;

    public PosAndZoomStorage(BlockPos te_pos) {
        pos = te_pos;
        this.zoom = new Vector2i(DEFAULT_ZOOM);
    }

    public PosAndZoomStorage() {
        pos = new BlockPos(0,0,0);
        zoom = new Vector2i(DEFAULT_ZOOM);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        System.out.println("position written :"+pos);
        tag.putInt("x",pos.getX());
        tag.putInt("y",pos.getY());
        tag.putInt("z",pos.getZ());
        tag.putInt("zoomX",zoom.x);
        tag.putInt("zoomY",zoom.y);
        return tag;
    }

    @Nonnull
    public BlockPos getBasePos() {
        return pos;
    }

    public void setBasePos(BlockPos pos_in) {
        pos = pos_in;
    }
    public void setBasePos(Direction.Axis axis, int newPos) {
        this.pos = this.pos.relative(axis,newPos - this.pos.get(axis));
    }

    @Nonnull
    public Vector2i getZoom() {
        return zoom;
    }

    public void setZoomX(int x) {
        this.zoom.x = x;
    }

    public void setZoomY(int y) {
        this.zoom.y = y;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("x") &&nbt.contains("y")  && nbt.contains("z") ) {
            int x = nbt.getInt("x");
            int y = nbt.getInt("y");
            int z = nbt.getInt("z");
            pos = new BlockPos(x,y,z);
            if (!nbt.contains("zoomX") || !nbt.contains("zoomY")){
                zoom = new Vector2i(DEFAULT_ZOOM);
            }else {
                int zoomX = nbt.getInt("zoomX");
                int zoomY = nbt.getInt("zoomY");
                zoom = new Vector2i(zoomX, zoomY);
            }
            System.out.println("position read :"+pos);
        }
    }


}
