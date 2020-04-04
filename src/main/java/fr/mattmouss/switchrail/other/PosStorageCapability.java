package fr.mattmouss.switchrail.other;

import fr.mattmouss.switchrail.switchdata.SwitchData;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.List;

public class PosStorageCapability
{
    @CapabilityInject(IPosStorage.class)
    public static Capability<IPosStorage> POS_STORAGE_CAPABILITY = null ;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IPosStorage.class, new Capability.IStorage<IPosStorage>() {

            @Override
            public INBT writeNBT(Capability<IPosStorage> capability, IPosStorage instance, Direction side)
            {
                BlockPos pos = instance.getPos();
                CompoundNBT nbt = new CompoundNBT();
                nbt.putInt("x",pos.getX());
                nbt.putInt("y",pos.getY());
                nbt.putInt("z",pos.getZ());
                return nbt;
            }

            @Override
            public void readNBT(Capability<IPosStorage> capability, IPosStorage instance, Direction side, INBT nbt) {
                if (!(instance instanceof PosStorage))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT compoundNBT = (CompoundNBT)nbt;
                int posX = compoundNBT.getInt("x");
                int posY = compoundNBT.getInt("y");
                int posZ = compoundNBT.getInt("z");
                instance.setPos(new BlockPos(posX,posY,posZ));
            }
        }, PosStorage::new);

    }

}
