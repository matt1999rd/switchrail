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

public class SwitchStorageCapability
{
    @CapabilityInject(ISwitchStorage.class)
    public static Capability<ISwitchStorage> SWITCH_STORAGE_CAPABILITY = null ;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ISwitchStorage.class, new Capability.IStorage<ISwitchStorage>() {

            @Override
            public INBT writeNBT(Capability<ISwitchStorage> capability, ISwitchStorage instance, Direction side)
            {
                ListNBT switches = new ListNBT();
                List<SwitchData> datas = instance.getSwitchList();
                System.out.println("writeNBT de Capability : nombre de switch :"+instance.getSwitchList().size());
                for (SwitchData data : datas){
                    CompoundNBT tag_switch = new CompoundNBT();
                    BlockPos pos =data.pos;
                    tag_switch.putInt("posX",pos.getX());
                    tag_switch.putInt("posY",pos.getY());
                    tag_switch.putInt("posZ",pos.getZ());
                    tag_switch.putString("type",data.type.getName());
                    switches.add(tag_switch);
                }
                return switches;
            }

            @Override
            public void readNBT(Capability<ISwitchStorage> capability, ISwitchStorage instance, Direction side, INBT nbt) {
                if (!(instance instanceof SwitchStorage))
                    throw new IllegalArgumentException("Can not deserialize to an instance that isn't the default implementation");
                ListNBT List_tag= (ListNBT)nbt;
                int max = List_tag.size();
                System.out.println("readNBT de Capability : nombre de switch :"+max);
                for (int i=0;i<max;i++){
                    CompoundNBT tag_switch = List_tag.getCompound(i);
                    int posX = tag_switch.getInt("posX");
                    int posY = tag_switch.getInt("posY");
                    int posZ = tag_switch.getInt("posZ");
                    String type = tag_switch.getString("type");
                    SwitchData data =  new SwitchData(SwitchType.valueOf(type),new BlockPos(posX,posY,posZ));
                    ((SwitchStorage)instance).addSwitchWhatever(data);
                }

            }
        },SwitchStorage::new);

    }

}
