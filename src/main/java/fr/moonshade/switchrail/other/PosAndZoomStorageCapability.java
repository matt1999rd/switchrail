package fr.moonshade.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;


public class PosAndZoomStorageCapability
{
    @CapabilityInject(PosAndZoomStorage.class)
    public static Capability<PosAndZoomStorage> POS_AND_ZOOM_STORAGE_CAPABILITY = null ;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(PosAndZoomStorage.class, new Capability.IStorage<PosAndZoomStorage>() {

            @Override
            public INBT writeNBT(Capability<PosAndZoomStorage> capability, PosAndZoomStorage instance, Direction side)
            {
                BlockPos pos = instance.getBasePos();
                Vector2i zoom = instance.getZoom();
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("x",pos.getX());
                nbt.putInt("y",pos.getY());
                nbt.putInt("z",pos.getZ());
                nbt.putInt("zoomX", zoom.x);
                nbt.putInt("zoomY", zoom.y);
                return nbt;
            }

            @Override
            public void readNBT(Capability<PosAndZoomStorage> capability, PosAndZoomStorage instance, Direction side, INBT nbt) {
                if (instance == null)
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT compoundNBT = (CompoundNBT)nbt;
                int posX = compoundNBT.getInt("x");
                int posY = compoundNBT.getInt("y");
                int posZ = compoundNBT.getInt("z");
                int zoomX = compoundNBT.getInt("zoomX");
                int zoomY = compoundNBT.getInt("zoomY");
                instance.setBasePos(new BlockPos(posX,posY,posZ));
                instance.setZoomX(zoomX);
                instance.setZoomY(zoomY);
            }
        }, PosAndZoomStorage::new);

    }

}
