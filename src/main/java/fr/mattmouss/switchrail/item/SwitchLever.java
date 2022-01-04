package fr.mattmouss.switchrail.item;

import fr.mattmouss.switchrail.switchblock.Switch;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;



import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;

import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.BlockPos;

import net.minecraft.world.World;


public class SwitchLever extends Item {

    private static final Rarity rarity = Rarity.UNCOMMON;


    public SwitchLever(Properties builder) {
        super(builder.stacksTo(1).rarity(rarity));
        this.setRegistryName("switch_lever");
    }



    @Override
    public ActionResultType useOn(ItemUseContext context) {

        BlockPos pos=context.getClickedPos();

        World world = context.getLevel();

        PlayerEntity player= context.getPlayer();

        Block clickedBlock = world.getBlockState(pos).getBlock();

        if ((clickedBlock instanceof Switch) && !world.isClientSide){
            Switch sw = (Switch)clickedBlock;
            BlockState state = sw.getBlockState(world,pos);
            if (state != null) {
                System.out.println("item lever use successfully");
                sw.updatePoweredState(world, state, pos,player,7,false);
                return ActionResultType.SUCCESS;
            }
            return ActionResultType.FAIL;

        }


        return ActionResultType.FAIL;
    }

}
