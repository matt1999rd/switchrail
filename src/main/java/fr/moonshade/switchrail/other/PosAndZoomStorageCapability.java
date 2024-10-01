package fr.moonshade.switchrail.other;

//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.Tag;
//import net.minecraft.core.Direction;
//import net.minecraft.core.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
//import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;


public class PosAndZoomStorageCapability
{
    public static Capability<PosAndZoomStorage> POS_AND_ZOOM_STORAGE_CAPABILITY = CapabilityManager.get(new CapabilityToken<>() {});

    public PosAndZoomStorageCapability() {
    }
    public static void register(RegisterCapabilitiesEvent event) {
        event.register(PosAndZoomStorage.class);
    }
    /*
    public static class Storage implements Capability.
                , new Capability.IStorage<PosAndZoomStorage>() {

            @Override
            public Tag writeNBT(Capability<PosAndZoomStorage> capability, PosAndZoomStorage instance, Direction side)
            {
                BlockPos pos = instance.getBasePos();
                Vector2i zoom = instance.getZoom();
                CompoundTag nbt = new CompoundTag();
                nbt.putInt("x",pos.getX());
                nbt.putInt("y",pos.getY());
                nbt.putInt("z",pos.getZ());
                nbt.putInt("zoomX", zoom.x);
                nbt.putInt("zoomY", zoom.y);
                return nbt;
            }

            @Override
            public void readNBT(Capability<PosAndZoomStorage> capability, PosAndZoomStorage instance, Direction side, Tag nbt) {
                if (instance == null)
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundTag compoundNBT = (CompoundTag)nbt;
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
    */


}
