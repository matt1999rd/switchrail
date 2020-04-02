package fr.mattmouss.switchrail.other;

import fr.mattmouss.switchrail.switchblock.Switch;
import fr.mattmouss.switchrail.switchdata.SwitchData;
import fr.mattmouss.switchrail.enum_rail.SwitchType;
import net.minecraft.block.Block;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;


import java.util.ArrayList;
import java.util.List;

public class SwitchStorage implements INBTSerializable<CompoundNBT>,ISwitchStorage{

    private List<SwitchData> switchList;

    public SwitchStorage() {
        switchList = new ArrayList<>();
    }

    public SwitchStorage(World world) {
        switchList = new ArrayList<>();
    }



    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT tag = new CompoundNBT();
        ListNBT switches = new ListNBT();
        for (SwitchData data : switchList){
            CompoundNBT tag_switch = new CompoundNBT();
            BlockPos pos =data.pos;
            tag_switch.putInt("posX",pos.getX());
            tag_switch.putInt("posY",pos.getY());
            tag_switch.putInt("posZ",pos.getZ());
            tag_switch.putString("type",data.type.getName());
            switches.add(tag_switch);
        }
        tag.put("switch_list",switches);
        return tag;
    }

    public List<SwitchData> getSwitchList() {
        return switchList;
    }




    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        ListNBT switch_List= (ListNBT) nbt.get("switch_list");
        int max = switch_List.size();
        if (max !=0) {
            for (int i = 0; i < max; i++) {
                CompoundNBT tag_switch = switch_List.getCompound(i);
                int posX = tag_switch.getInt("posX");
                int posY = tag_switch.getInt("posY");
                int posZ = tag_switch.getInt("posZ");
                String type = tag_switch.getString("type");
                SwitchData data = new SwitchData(SwitchType.valueOf(type),new BlockPos(posX,posY,posZ));
                if (!switchList.contains(data)) {
                    switchList.add(data);
                }
            }
        }


    }

    public void addSwitch(SwitchData data,World world){
        if (isNotInList(data) && exists(data,world)){
            switchList.add(data);
        } else {
            System.out.println("**ERROR** : switch already registered !! or Block does not exist anymore");
        }
    }

    public void addSwitchWhatever(SwitchData data){
        if (isNotInList(data)){
            switchList.add(data);
        } else {
            System.out.println("**ERROR** : switch already registered !! ");
        }
    }

    public void deleteSwitch(SwitchData data){
        System.out.println("deleteSwitch");
        if (!isNotInList(data)){
            switchList.remove(getIndex(data));
            System.out.println(switchList.size());
        } else {
            System.out.println("le block n'existe pas !!");
        }
    }

    public Boolean isNotInList(SwitchData data) {
        /*
        private boolean isNotDoubloon(SwitchTile switchTile) {
            AtomicReference<Boolean> result = null;
            switches.ifPresent(switchStorage -> result.set(switchStorage.isNotDoubloon(switchTile)));
            return result.get();

        for (SwitchTile tile : switches){
            BlockPos pos_in = tile.getPos();
            if (pos_in.getX() == pos.getX() && pos_in.getY() == pos.getY() && pos_in.getZ() == pos.getZ()){
                return false;
            }
        }
        return true;


        System.out.println("** isNotInList **");
        System.out.println("SwitchData à tester : "+data);

        System.out.println("--------------------------------------------");
        */
        for (SwitchData data_in : switchList){
            //System.out.println("SwitchData en cours de test dans SwitchList : "+data_in);
            BlockPos pos = data.pos;
            //System.out.println("Position du block à tester :" +pos);
            BlockPos pos_it = data_in.pos;
            //System.out.println("Position du block de test dans SwitchList :" +pos);
            if (pos_it.getX() == pos.getX() && pos_it.getY() == pos.getY() && pos_it.getZ() == pos.getZ()){
                //System.out.println("echec !!");
                return false;
            }
        }
        return true;
    }

    public int getIndex(SwitchData data) {

        for (SwitchData data_in : switchList){
            //System.out.println("SwitchData en cours de test dans SwitchList : "+data_in);
            BlockPos pos = data.pos;
            //System.out.println("Position du block à tester :" +pos);
            BlockPos pos_it = data_in.pos;
            //System.out.println("Position du block de test dans SwitchList :" +pos);
            if (pos_it.getX() == pos.getX() && pos_it.getY() == pos.getY() && pos_it.getZ() == pos.getZ()){
                //System.out.println("echec !!");
                return switchList.indexOf(data_in);
            }
        }
        return -1;
    }



    private Boolean exists(SwitchData data,World world){
        BlockPos pos = data.pos;
        SwitchType type = data.type;
        Block block = world.getTileEntity(pos).getBlockState().getBlock();
        return (block instanceof Switch) && (((Switch)block).getType() == type ); // on a bien une instance de Switch avec le bon type
    }

}
