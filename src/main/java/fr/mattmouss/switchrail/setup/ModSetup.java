package fr.mattmouss.switchrail.setup;

import fr.mattmouss.switchrail.blocks.ModBlock;
import fr.mattmouss.switchrail.network.Networking;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;

public class ModSetup {


    public ItemGroup itemGroup = new ItemGroup("switchrail") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ModBlock.switchStraightVRight);
        }

    };




    public void init(){
        Networking.registerMessages();
    }
}
