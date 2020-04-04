package fr.mattmouss.switchrail.blocks;

import fr.mattmouss.switchrail.gui.ControllerContainer;
import fr.mattmouss.switchrail.other.PosStorage;
import fr.mattmouss.switchrail.other.PosStorageCapability;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ControllerTile extends TileEntity implements ITickableTileEntity, INamedContainerProvider {
    //on ne peut pas stocker les tile entity car pour les recuperer il faut un world qui n'est pas encore defini lorsque le world est en train de se charger

    //solution : on va stocker les données propres à chaque switch dans un nouvel objet SwitchData qui sera envoyé par petit bout via PosStorage

    private LazyOptional<PosStorage> pos_store = LazyOptional.of(this::createPos).cast();

    private Boolean isInit = true;




    public ControllerTile() {
        super(ModBlock.CONTROLLER_TILE);
    }

    @Override
    public void tick() {
        if (isInit){
            isInit = false;
        }

    }

    private PosStorage createPos(){
        return new PosStorage(this.pos);
    }


    public BlockPos getPosBase() {
        AtomicIntegerArray integerArray = new AtomicIntegerArray(3);
        pos_store.ifPresent(posStorage -> {
            BlockPos pos = posStorage.getPos();
            integerArray.set(0,pos.getX());
            integerArray.set(1,pos.getY());
            integerArray.set(2,pos.getZ());
        });
        return new BlockPos(integerArray.get(0),integerArray.get(1),integerArray.get(2));
    }

    public void setX(int x){
        pos_store.ifPresent(posStorage -> posStorage.setX(x));
    }

    public void setY(int y){
        pos_store.ifPresent(posStorage -> posStorage.setY(y));
    }

    public void setZ(int z){
        pos_store.ifPresent(posStorage -> posStorage.setZ(z));
    }
    /*
    public void addSwitch(SwitchTile switch_tile){
        switches.ifPresent(switchStorage -> switchStorage.addSwitch(new SwitchData(switch_tile),world));
    }

    public void deleteSwitch(SwitchTile switch_tile){
        System.out.println("deleteSwitch");
        switches.ifPresent(switchStorage -> switchStorage.deleteSwitch(new SwitchData(switch_tile)));
    }

    public List<SwitchData> getSwitch() {
        List<SwitchData> datas = new ArrayList<>();
        List<SwitchData> datas2 = getCapability(PosStorageCapability.SWITCH_STORAGE_CAPABILITY).map(IPosStorage::getSwitchList).orElse(datas);
        return datas2;
    }

     */

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
        CompoundNBT pos_tag = compound.getCompound("pos");
        pos_store.ifPresent(switchStorage -> ((INBTSerializable<CompoundNBT>)switchStorage).deserializeNBT(pos_tag));
        super.read(compound);

    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        pos_store.ifPresent(posStorage -> {
            CompoundNBT compoundNBT = ((INBTSerializable<CompoundNBT>)posStorage).serializeNBT();
            tag.put("pos",compoundNBT);
        });
        return super.write(tag);
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == PosStorageCapability.POS_STORAGE_CAPABILITY){
            return pos_store.cast();
        }
        return super.getCapability(cap, side);
    }
}
