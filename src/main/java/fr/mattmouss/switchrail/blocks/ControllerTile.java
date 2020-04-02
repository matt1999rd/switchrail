package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.gui.ControllerContainer;
import fr.mattmouss.switchrail.gui.ControllerScreen;
import fr.mattmouss.switchrail.other.ISwitchStorage;
import fr.mattmouss.switchrail.other.SwitchStorage;
import fr.mattmouss.switchrail.other.SwitchStorageCapability;
import fr.mattmouss.switchrail.switchdata.SwitchData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ControllerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    //on ne peut pas stocker les tile entity car pour les recuperer il faut un world qui n'est pas encore defini lorsque le world est en train de se charger

    //solution : on va stocker les données propres à chaque switch dans un nouvel objet SwitchData qui sera envoyé par petit bout via SwitchStorage

    private LazyOptional<SwitchStorage> switches = LazyOptional.of(this::createSwitch).cast();

    public BlockPos pos_base ;

    private Boolean isInit = true;




    public ControllerTile() {
        super(ModBlock.CONTROLLER_TILE);
    }

    @Override
    public void tick() {
        if (isInit){
            pos_base = this.pos;
            isInit = false;
        }

    }

    private SwitchStorage createSwitch(){
        return new SwitchStorage(world);
    }


    public void addSwitch(SwitchTile switch_tile){
        switches.ifPresent(switchStorage -> switchStorage.addSwitch(new SwitchData(switch_tile),world));
    }

    public void deleteSwitch(SwitchTile switch_tile){
        System.out.println("deleteSwitch");
        switches.ifPresent(switchStorage -> switchStorage.deleteSwitch(new SwitchData(switch_tile)));
    }

    public List<SwitchData> getSwitch() {
        List<SwitchData> datas = new ArrayList<>();
        List<SwitchData> datas2 = getCapability(SwitchStorageCapability.SWITCH_STORAGE_CAPABILITY).map(ISwitchStorage::getSwitchList).orElse(datas);
        return datas2;
    }

    @Override
    public ITextComponent getDisplayName() {
        return new StringTextComponent(getType().getRegistryName().getPath());
    }

    @Nullable
    @Override
    public Container createMenu(int i, PlayerInventory inventory, PlayerEntity playerEntity) {
        return new ControllerContainer(i,world,pos,playerEntity);
    }

    @Override
    public void read(CompoundNBT compound) {
        CompoundNBT switch_tag = compound.getCompound("swag");
        switches.ifPresent(switchStorage -> ((INBTSerializable<CompoundNBT>)switchStorage).deserializeNBT(switch_tag));
        super.read(compound);

    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        switches.ifPresent(switchStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)switchStorage).serializeNBT();
            tag.put("swag",compoundNBT);
        });
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == SwitchStorageCapability.SWITCH_STORAGE_CAPABILITY){
            return switches.cast();
        }
        return super.getCapability(cap, side);
    }
}
