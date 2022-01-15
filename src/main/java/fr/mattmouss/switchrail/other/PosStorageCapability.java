package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;


public class PosStorageCapability
{
    @CapabilityInject(PosStorage.class)
    public static Capability<PosStorage> POS_STORAGE_CAPABILITY = null ;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(PosStorage.class, new Capability.IStorage<PosStorage>() {

            @Override
            public INBT writeNBT(Capability<PosStorage> capability, PosStorage instance, Direction side)
            {
                BlockPos pos = instance.getBasePos();
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("x",pos.getX());
                nbt.putInt("y",pos.getY());
                nbt.putInt("z",pos.getZ());
                return nbt;
            }

            @Override
            public void readNBT(Capability<PosStorage> capability, PosStorage instance, Direction side, INBT nbt) {
                if (instance == null)
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT compoundNBT = (CompoundNBT)nbt;
                int posX = compoundNBT.getInt("x");
                int posY = compoundNBT.getInt("y");
                int posZ = compoundNBT.getInt("z");
                instance.setBasePos(new BlockPos(posX,posY,posZ));
            }
        }, PosStorage::new);

    }

}
