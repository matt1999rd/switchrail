package fr.mattmouss.switchrail.other;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import java.util.Map;


public class TerminalStorageCapability
{
    @CapabilityInject(ITerminalStorage.class)
    public static Capability<ITerminalStorage> TERMINAL_STORAGE_CAPABILITY = null ;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ITerminalStorage.class, new Capability.IStorage<ITerminalStorage>() {

            @Override
            public INBT writeNBT(Capability<ITerminalStorage> capability, ITerminalStorage instance, Direction side)
            {
                CompoundNBT nbt = new CompoundNBT();
                ListNBT listNBT = new ListNBT();
                Map<BlockPos,Byte> switchMap = instance.getSwitchMap();
                for (BlockPos pos : switchMap.keySet()){
                    CompoundNBT switchNBT = new CompoundNBT();
                    Util.putPos(switchNBT,pos);
                    switchNBT.putByte("sw_position",switchMap.get(pos));
                    listNBT.add(switchNBT);
                }
                nbt.put("list",listNBT);
                Util.putPos(nbt,instance.getBasePos());
                return listNBT;
            }

            @Override
            public void readNBT(Capability<ITerminalStorage> capability, ITerminalStorage instance, Direction side, INBT nbt) {
                if (!(instance instanceof TerminalStorage))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                CompoundNBT compoundNBT = (CompoundNBT)nbt;
                ListNBT listNBT = (ListNBT) compoundNBT.get("list");
                for (INBT inbt : listNBT){
                    CompoundNBT sw_nbt = (CompoundNBT) inbt;
                    BlockPos pos = Util.getPosFromNbt(sw_nbt);
                    byte switchPosition = sw_nbt.getByte("sw_position");
                    instance.addSwitch(pos);
                    instance.setSwitchPosition(pos,switchPosition);
                }
                instance.setBasePos(Util.getPosFromNbt(compoundNBT));
            }
        }, TerminalStorage::new);

    }

}
